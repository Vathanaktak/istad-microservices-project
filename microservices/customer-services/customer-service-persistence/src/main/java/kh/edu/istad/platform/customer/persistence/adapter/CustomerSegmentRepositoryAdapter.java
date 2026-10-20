package kh.edu.istad.platform.customer.persistence.adapter;

import kh.edu.istad.platform.customer.domain.port.out.CustomerSegmentRepository;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerSegment;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerSegmentFilterCriteria;
import kh.edu.istad.platform.customer.persistence.document.CustomerSegmentDocument;
import kh.edu.istad.platform.customer.persistence.mapper.CustomerPersistenceMapper;
import kh.edu.istad.platform.customer.persistence.repository.CustomerSegmentMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomerSegmentRepositoryAdapter implements CustomerSegmentRepository {

    private final CustomerSegmentMongoRepository customerSegmentMongoRepository;
    private final CustomerPersistenceMapper customerPersistenceMapper;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Override
    public Mono<CustomerSegment> save(CustomerSegment segment) {
        CustomerSegmentDocument doc = customerPersistenceMapper.mapSegmentToDocument(segment);
        return customerSegmentMongoRepository.save(doc)
                .map(customerPersistenceMapper::mapDocumentToSegment);
    }

    @Override
    public Mono<CustomerSegment> findById(String id) {
        return customerSegmentMongoRepository.findById(id)
                .map(customerPersistenceMapper::mapDocumentToSegment);
    }

    @Override
    public Mono<CustomerSegment> findByNameIgnoreCase(String name) {
        return customerSegmentMongoRepository.findByNameIgnoreCase(name)
                .map(customerPersistenceMapper::mapDocumentToSegment);
    }

    @Override
    public Flux<CustomerSegment> findAll() {
        return customerSegmentMongoRepository.findAll()
                .map(customerPersistenceMapper::mapDocumentToSegment);
    }

    @Override
    public Flux<CustomerSegment> filter(CustomerSegmentFilterCriteria criteria) {
        if (criteria == null || criteria.isEmpty()) {
            return findAll();
        }

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        if (criteria.id() != null && !criteria.id().isBlank()) {
            criteriaList.add(Criteria.where("_id")
                    .regex(Pattern.quote(criteria.id().trim()), "i"));
        }

        if (criteria.name() != null && !criteria.name().isBlank()) {
            criteriaList.add(Criteria.where("name")
                    .regex(Pattern.quote(criteria.name().trim()), "i"));
        }

        if (criteria.description() != null && !criteria.description().isBlank()) {
            criteriaList.add(Criteria.where("description")
                    .regex(Pattern.quote(criteria.description().trim()), "i"));
        }

        if (criteria.query() != null && !criteria.query().isBlank()) {
            String q = Pattern.quote(criteria.query().trim());
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("_id").regex(q, "i"),
                    Criteria.where("name").regex(q, "i"),
                    Criteria.where("description").regex(q, "i")
            ));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        query.with(Sort.by(Sort.Direction.ASC, "name"));

        return reactiveMongoTemplate.find(query, CustomerSegmentDocument.class)
                .map(customerPersistenceMapper::mapDocumentToSegment);
    }

    @Override
    public Mono<Boolean> existsById(String id) {
        return customerSegmentMongoRepository.existsById(id);
    }

    @Override
    public Mono<Boolean> existsByNameIgnoreCase(String name) {
        return customerSegmentMongoRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public Mono<Boolean> isSegmentInUse(String segmentId, String segmentName) {
        List<Object> targets = new ArrayList<>();
        if (segmentId != null) {
            targets.add(segmentId);
        }
        if (segmentName != null) {
            targets.add(segmentName);
            targets.add("SEG-" + segmentName.toUpperCase());
        }

        Criteria criteria = new Criteria().orOperator(
                Criteria.where("customerSegment").in(targets),
                Criteria.where("customerSegment.$id").in(targets),
                Criteria.where("customerSegment._id").in(targets),
                Criteria.where("customerSegment.id").in(targets)
        );
        return reactiveMongoTemplate.exists(Query.query(criteria), "customers");
    }

    @Override
    public Mono<Boolean> deleteById(String id) {
        return customerSegmentMongoRepository.deleteById(id).thenReturn(true);
    }
}
