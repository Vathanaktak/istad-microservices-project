package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.platform.customer.domain.dto.CustomerSegmentResult;
import kh.edu.istad.platform.customer.domain.mapper.CustomerDataMapper;
import kh.edu.istad.platform.customer.domain.port.out.CustomerSegmentRepository;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerSegmentFilterCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@Slf4j
@RequiredArgsConstructor
public class InitiateFindAllCustomerSegmentsUseCase {

    private final CustomerSegmentRepository customerSegmentRepository;
    private final CustomerDataMapper customerDataMapper;

    public Flux<CustomerSegmentResult> findAll(CustomerSegmentFilterCriteria criteria) {
        log.info("Find all customer segments with filter: {}", criteria);
        return customerSegmentRepository.filter(criteria)
                .map(customerDataMapper::toCustomerSegmentResult);
    }

    public Flux<CustomerSegmentResult> findAll() {
        return findAll(new CustomerSegmentFilterCriteria(null, null, null, null));
    }
}
