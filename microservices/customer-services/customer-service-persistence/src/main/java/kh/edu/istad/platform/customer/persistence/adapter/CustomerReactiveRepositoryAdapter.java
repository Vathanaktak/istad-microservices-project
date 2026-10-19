package kh.edu.istad.platform.customer.persistence.adapter;

import kh.edu.istad.common.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.dto.CustomerStatusReportResult;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerFilterCriteria;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerSegment;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerStatus;
import kh.edu.istad.platform.customer.persistence.document.CustomerDocument;
import kh.edu.istad.platform.customer.persistence.document.CustomerSegmentDocument;
import kh.edu.istad.platform.customer.persistence.dto.CustomerStatusAggregationDto;
import kh.edu.istad.platform.customer.persistence.mapper.CustomerPersistenceMapper;
import kh.edu.istad.platform.customer.persistence.repository.CustomerMongoRepository;
import kh.edu.istad.platform.customer.persistence.repository.CustomerSegmentMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

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
    public Customer save(Customer customer) {
        CustomerDocument document = customerPersistenceMapper.toDocument(customer);
        if (document.getId() != null) {
            CustomerDocument existing = customerMongoRepository.findById(document.getId()).block();
            if (existing != null) {
                document.setCreateAt(existing.getCreateAt());
                document.setCreateBy(existing.getCreateBy());
            }
        }
        if (document.getCustomerSegment() != null) {
            customerSegmentMongoRepository.save(document.getCustomerSegment()).block();
        }
        CustomerDocument saved = customerMongoRepository.save(document).block();
        Customer domain = customerPersistenceMapper.toDomain(saved);
        if (domain.getCustomerSegment() == null) {
            CustomerSegment seg = customer.getCustomerSegment() != null
                    ? customer.getCustomerSegment()
                    : CustomerSegment.SILVER;
            domain.updateCustomerSegment(seg);
        }
        return domain;
    }

    @Override
    public Optional<Customer> findById(CustomerId customerId) {
        if (customerId == null || customerId.value() == null) {
            return Optional.empty();
        }
        Query query = new Query(Criteria.where("_id").is(customerId.value().toString()));
        List<Customer> list = fetchCustomers(query);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<Customer> findAll() {
        return fetchCustomers(new Query());
    }

    @Override
    public List<Customer> filter(CustomerFilterCriteria filter) {
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

    private List<Customer> fetchCustomers(Query query) {
        List<org.bson.Document> rawDocs = reactiveMongoTemplate.find(query, org.bson.Document.class, "customers")
                .collectList()
                .block();

        if (rawDocs == null || rawDocs.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, CustomerSegmentDocument> segmentMap = customerSegmentMongoRepository.findAll()
                .collectMap(CustomerSegmentDocument::getId)
                .block();

        List<Customer> result = new ArrayList<>();
        for (org.bson.Document rawDoc : rawDocs) {
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

            result.add(customerPersistenceMapper.toDomain(customerDoc));
        }

        return result;
    }

    @Override
    public List<CustomerStatusReportResult> getCustomerStatusReport() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("status").ne(null)),
                Aggregation.group("status").count().as("total"),
                Aggregation.project("total").and("_id").as("status")
        );

        List<CustomerStatusAggregationDto> aggResults = reactiveMongoTemplate
                .aggregate(aggregation, "customers", CustomerStatusAggregationDto.class)
                .collectList()
                .block();

        Map<String, Long> countMap = new LinkedHashMap<>();
        for (CustomerStatus status : CustomerStatus.values()) {
            countMap.put(status.name(), 0L);
        }

        if (aggResults != null) {
            for (CustomerStatusAggregationDto dto : aggResults) {
                String resolvedStatus = dto.getResolvedStatus();
                if (resolvedStatus != null) {
                    countMap.put(resolvedStatus, dto.getTotal());
                }
            }
        }

        return countMap.entrySet().stream()
                .map(entry -> new CustomerStatusReportResult(entry.getKey(), entry.getValue()))
                .toList();
    }
}
