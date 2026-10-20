package kh.edu.istad.platform.customer.domain.port.out;

import kh.edu.istad.platform.customer.domain.valueObject.CustomerSegment;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerSegmentFilterCriteria;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerSegmentRepository {
    Mono<CustomerSegment> save(CustomerSegment segment);
    Mono<CustomerSegment> findById(String id);
    Mono<CustomerSegment> findByNameIgnoreCase(String name);
    Flux<CustomerSegment> findAll();
    Flux<CustomerSegment> filter(CustomerSegmentFilterCriteria criteria);
    Mono<Boolean> existsById(String id);
    Mono<Boolean> existsByNameIgnoreCase(String name);
    Mono<Boolean> isSegmentInUse(String segmentId, String segmentName);
    Mono<Boolean> deleteById(String id);
}
