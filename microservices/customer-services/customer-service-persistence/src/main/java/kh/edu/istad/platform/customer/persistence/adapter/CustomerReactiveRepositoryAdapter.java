package kh.edu.istad.platform.customer.persistence.adapter;

import kh.edu.istad.common.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.dto.CustomerStatisticsResult;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerFilterCriteria;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerSegment;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerStatus;
import kh.edu.istad.platform.customer.persistence.document.CustomerDocument;
import kh.edu.istad.platform.customer.persistence.document.CustomerSegmentDocument;
import kh.edu.istad.platform.customer.persistence.dto.CustomerStatusSegmentAggregationDto;
import kh.edu.istad.platform.customer.persistence.mapper.CustomerPersistenceMapper;
import kh.edu.istad.platform.customer.persistence.repository.CustomerMongoRepository;
import kh.edu.istad.platform.customer.persistence.repository.CustomerSegmentMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class CustomerReactiveRepositoryAdapter implements CustomerRepository {

    private final CustomerMongoRepository customerMongoRepository;
    private final CustomerSegmentMongoRepository customerSegmentMongoRepository;
    private final CustomerPersistenceMapper customerPersistenceMapper;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Override
    public Mono<Customer> save(Customer customer) {
        CustomerDocument document = customerPersistenceMapper.toDocument(customer);

        Mono<CustomerDocument> prepareDocMono = (document.getId() != null)
                ? customerMongoRepository.findById(document.getId())
                        .doOnNext(existing -> {
                            document.setCreateAt(existing.getCreateAt());
                            document.setCreateBy(existing.getCreateBy());
                        })
                        .thenReturn(document)
                        .defaultIfEmpty(document)
                : Mono.just(document);

        Mono<Void> saveSegmentMono = (document.getCustomerSegment() != null)
                ? customerSegmentMongoRepository.save(document.getCustomerSegment()).then()
                : Mono.empty();

        return prepareDocMono
                .flatMap(doc -> saveSegmentMono.thenReturn(doc))
                .flatMap(doc -> customerMongoRepository.save(doc))
                .map(saved -> {
                    Customer domain = customerPersistenceMapper.toDomain(saved);
                    if (domain.getCustomerSegment() == null) {
                        CustomerSegment seg = customer.getCustomerSegment() != null
                                ? customer.getCustomerSegment()
                                : CustomerSegment.SILVER;
                        domain.updateCustomerSegment(seg);
                    }
                    return domain;
                });
    }

    @Override
    public Mono<Customer> findById(CustomerId customerId) {
        if (customerId == null || customerId.value() == null) {
            return Mono.empty();
        }
        Query query = new Query(Criteria.where("_id").is(customerId.value().toString()));
        return fetchCustomers(query).next();
    }

    @Override
    public Flux<Customer> findAll() {
        return fetchCustomers(new Query());
    }

    @Override
    public Flux<Customer> filter(CustomerFilterCriteria filter) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        if (filter.name() != null && !filter.name().isBlank()) {
            String namePattern = Pattern.quote(filter.name().trim());
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("username").regex(namePattern, "i"),
                    Criteria.where("familyName").regex(namePattern, "i"),
                    Criteria.where("givenName").regex(namePattern, "i")
            ));
        }

        if (filter.email() != null && !filter.email().isBlank()) {
            criteriaList.add(Criteria.where("email")
                    .regex(Pattern.quote(filter.email().trim()), "i"));
        }

        if (filter.phoneNumber() != null && !filter.phoneNumber().isBlank()) {
            criteriaList.add(Criteria.where("phoneNumber")
                    .regex(Pattern.quote(filter.phoneNumber().trim()), "i"));
        }

        if (filter.status() != null && !filter.status().isBlank()) {
            try {
                CustomerStatus statusEnum = CustomerStatus.valueOf(filter.status().trim().toUpperCase());
                criteriaList.add(Criteria.where("status").is(statusEnum));
            } catch (IllegalArgumentException ignored) {
                criteriaList.add(Criteria.where("status").is(filter.status().trim()));
            }
        }

        if (filter.customerSegment() != null && !filter.customerSegment().isBlank()) {
            String segmentQuery = filter.customerSegment().trim();
            CustomerSegment known = CustomerSegment.fromName(segmentQuery);
            List<String> targetIds = new ArrayList<>();
            targetIds.add(known.id());
            targetIds.add(segmentQuery);
            targetIds.add("SEG-" + segmentQuery.toUpperCase());

            if ("Silver".equalsIgnoreCase(known.name())) {
                criteriaList.add(new Criteria().orOperator(
                        Criteria.where("customerSegment").in(targetIds),
                        Criteria.where("customerSegment").exists(false),
                        Criteria.where("customerSegment").is(null)
                ));
            } else {
                criteriaList.add(Criteria.where("customerSegment").in(targetIds));
            }
        }

        // Combine all active filters with AND
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        return fetchCustomers(query);
    }

    private Flux<Customer> fetchCustomers(Query query) {
        return customerSegmentMongoRepository.findAll()
                .collectMap(CustomerSegmentDocument::getId)
                .flatMapMany(segmentMap -> reactiveMongoTemplate.find(query, org.bson.Document.class, "customers")
                        .map(rawDoc -> {
                            CustomerDocument customerDoc = reactiveMongoTemplate.getConverter().read(CustomerDocument.class, rawDoc);

                            if (customerDoc.getCustomerSegment() == null) {
                                Object segRef = rawDoc.get("customerSegment");
                                String segId = segRef != null ? segRef.toString() : "SEG-SILVER";
                                CustomerSegmentDocument segDoc = segmentMap != null ? segmentMap.get(segId) : null;
                                if (segDoc == null) {
                                    CustomerSegment known = CustomerSegment.fromName(segId.replace("SEG-", ""));
                                    segDoc = new CustomerSegmentDocument(known.id(), known.name(), known.description());
                                }
                                customerDoc.setCustomerSegment(segDoc);
                            }

                            return customerPersistenceMapper.toDomain(customerDoc);
                        })
                );
    }

    @Override
    public Mono<CustomerStatisticsResult> getCustomerStatistics() {
        AggregationOperation rawSegmentOp = context ->
                new org.bson.Document("$addFields",
                        new org.bson.Document("rawSegment",
                                new org.bson.Document("$ifNull", List.of(
                                        "$customerSegment.$id",
                                        new org.bson.Document("$ifNull", List.of("$customerSegment._id", "$customerSegment"))
                                ))
                        )
                );

        AggregationOperation resolvedSegmentOp = context ->
                new org.bson.Document("$addFields",
                        new org.bson.Document("resolvedSegment",
                                new org.bson.Document("$ifNull", List.of("$rawSegment", "SEG-SILVER"))
                        )
                );

        AggregationOperation groupOp = context ->
                new org.bson.Document("$group",
                        new org.bson.Document("_id",
                                new org.bson.Document("status", "$status").append("segment", "$resolvedSegment")
                        ).append("total", new org.bson.Document("$sum", 1))
                );

        AggregationOperation sortOp = context ->
                new org.bson.Document("$sort",
                        new org.bson.Document("_id.status", 1).append("_id.segment", 1)
                );

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("status").ne(null)),
                rawSegmentOp,
                resolvedSegmentOp,
                groupOp,
                sortOp
        );

        return customerSegmentMongoRepository.findAll()
                .collectList()
                .flatMap(segmentDocs -> {
                    Map<String, String> segmentIdToName = new LinkedHashMap<>();
                    Map<String, String> segmentNameToId = new LinkedHashMap<>();

                    segmentIdToName.put("SEG-SILVER", "Silver");
                    segmentIdToName.put("SEG-GOLD", "Gold");
                    segmentIdToName.put("SEG-PREMIUM", "Premium");

                    for (CustomerSegmentDocument doc : segmentDocs) {
                        segmentIdToName.put(doc.getId(), doc.getName());
                        segmentNameToId.put(doc.getName().toUpperCase(), doc.getId());
                    }

                    return reactiveMongoTemplate.aggregate(aggregation, "customers", CustomerStatusSegmentAggregationDto.class)
                            .collectList()
                            .map(aggResults -> {
                                long totalCustomers = 0L;
                                Map<String, Long> statusCounts = new LinkedHashMap<>();
                                for (CustomerStatus s : CustomerStatus.values()) {
                                    statusCounts.put(s.name(), 0L);
                                }

                                Map<String, Long> segmentCounts = new LinkedHashMap<>();
                                segmentCounts.put("SEG-SILVER", 0L);
                                segmentCounts.put("SEG-GOLD", 0L);
                                segmentCounts.put("SEG-PREMIUM", 0L);

                                List<CustomerStatisticsResult.StatusSegmentCount> statusSegmentCounts = new ArrayList<>();

                                if (aggResults != null) {
                                    for (CustomerStatusSegmentAggregationDto item : aggResults) {
                                        if (item.getId() == null) continue;
                                        String rawStatus = item.getId().getStatus();
                                        String rawSeg = item.getId().getSegment();
                                        long total = item.getTotal();

                                        totalCustomers += total;

                                        if (rawStatus != null) {
                                            statusCounts.put(rawStatus, statusCounts.getOrDefault(rawStatus, 0L) + total);
                                        }

                                        String resolvedId = rawSeg;
                                        String resolvedName;

                                        if (resolvedId != null && segmentIdToName.containsKey(resolvedId)) {
                                            resolvedName = segmentIdToName.get(resolvedId);
                                        } else if (resolvedId != null && segmentNameToId.containsKey(resolvedId.toUpperCase())) {
                                            String realId = segmentNameToId.get(resolvedId.toUpperCase());
                                            resolvedName = segmentIdToName.getOrDefault(realId, resolvedId);
                                            resolvedId = realId;
                                        } else if (resolvedId != null && resolvedId.startsWith("SEG-")) {
                                            resolvedName = CustomerSegment.fromName(resolvedId.substring(4)).name();
                                        } else if (resolvedId != null) {
                                            CustomerSegment known = CustomerSegment.fromName(resolvedId);
                                            resolvedId = known.id();
                                            resolvedName = known.name();
                                        } else {
                                            resolvedId = "SEG-SILVER";
                                            resolvedName = "Silver";
                                        }

                                        segmentCounts.put(resolvedId, segmentCounts.getOrDefault(resolvedId, 0L) + total);

                                        statusSegmentCounts.add(new CustomerStatisticsResult.StatusSegmentCount(
                                                rawStatus != null ? rawStatus : "ACTIVE",
                                                resolvedId,
                                                resolvedName,
                                                total
                                        ));
                                    }
                                }

                                List<CustomerStatisticsResult.StatusCount> byStatus = statusCounts.entrySet().stream()
                                        .map(e -> new CustomerStatisticsResult.StatusCount(e.getKey(), e.getValue()))
                                        .toList();

                                List<CustomerStatisticsResult.SegmentCount> bySegment = segmentCounts.entrySet().stream()
                                        .map(e -> {
                                            String sId = e.getKey();
                                            String sName = segmentIdToName.getOrDefault(sId, sId.replace("SEG-", ""));
                                            return new CustomerStatisticsResult.SegmentCount(sId, sName, e.getValue());
                                        })
                                        .toList();

                                return new CustomerStatisticsResult(
                                        totalCustomers,
                                        byStatus,
                                        bySegment,
                                        statusSegmentCounts
                                );
                            });
                });
    }

    @Override
    public Mono<Boolean> deleteCustomerById(String id) {
        return customerMongoRepository.deleteById(id).thenReturn(true);
    }
}
