package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.common.exception.DomainException;
import kh.edu.istad.platform.customer.domain.dto.CustomerSegmentResult;
import kh.edu.istad.platform.customer.domain.mapper.CustomerDataMapper;
import kh.edu.istad.platform.customer.domain.port.out.CustomerSegmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class InitiateFindCustomerSegmentByIdUseCase {

    private final CustomerSegmentRepository customerSegmentRepository;
    private final CustomerDataMapper customerDataMapper;

    public Mono<CustomerSegmentResult> findById(String id) {
        if (id == null || id.isBlank()) {
            return Mono.error(new DomainException("Segment ID must not be blank"));
        }
        return customerSegmentRepository.findById(id.trim())
                .switchIfEmpty(customerSegmentRepository.findByNameIgnoreCase(id.trim()))
                .switchIfEmpty(Mono.error(new DomainException("Customer segment not found with id: " + id)))
                .map(customerDataMapper::toCustomerSegmentResult);
    }
}
