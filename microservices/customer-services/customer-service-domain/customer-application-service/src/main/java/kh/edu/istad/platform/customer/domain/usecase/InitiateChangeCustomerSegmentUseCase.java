package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.common.exception.DomainException;
import kh.edu.istad.common.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.dto.ChangeCustomerSegmentCommand;
import kh.edu.istad.platform.customer.domain.dto.InitiateCustomerResult;
import kh.edu.istad.platform.customer.domain.mapper.CustomerDataMapper;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.domain.port.out.CustomerSegmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class InitiateChangeCustomerSegmentUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerSegmentRepository customerSegmentRepository;
    private final CustomerDataMapper customerDataMapper;

    public Mono<InitiateCustomerResult> execute(ChangeCustomerSegmentCommand command) {
        log.info("Change customer segment: {}", command);

        if (command.customerId() == null) {
            return Mono.error(new DomainException("Customer ID must not be null"));
        }
        if (command.segmentId() == null || command.segmentId().isBlank()) {
            return Mono.error(new DomainException("Segment ID must not be blank"));
        }

        String segmentQuery = command.segmentId().trim();

        return customerSegmentRepository.findById(segmentQuery)
                .switchIfEmpty(customerSegmentRepository.findByNameIgnoreCase(segmentQuery))
                .switchIfEmpty(Mono.error(new DomainException("Customer segment not found: " + segmentQuery)))
                .flatMap(segment -> customerRepository.findById(new CustomerId(command.customerId()))
                        .switchIfEmpty(Mono.error(new DomainException("Customer not found with id: " + command.customerId())))
                        .flatMap(customer -> {
                            customer.updateCustomerSegment(segment);
                            return customerRepository.save(customer);
                        })
                )
                .map(customerDataMapper::toInitiateCustomerResult);
    }
}
