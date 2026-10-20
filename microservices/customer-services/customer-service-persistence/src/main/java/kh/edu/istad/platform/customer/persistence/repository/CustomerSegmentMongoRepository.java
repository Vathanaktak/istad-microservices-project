package kh.edu.istad.platform.customer.persistence.repository;

import kh.edu.istad.platform.customer.persistence.document.CustomerSegmentDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CustomerSegmentMongoRepository extends ReactiveMongoRepository<CustomerSegmentDocument, String> {
    Mono<CustomerSegmentDocument> findByNameIgnoreCase(String name);
    Mono<Boolean> existsByNameIgnoreCase(String name);
}
