package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.common.exception.DomainException;
import kh.edu.istad.platform.customer.domain.dto.CustomerSegmentResult;
import kh.edu.istad.platform.customer.domain.dto.UpdateCustomerSegmentCommand;
import kh.edu.istad.platform.customer.domain.mapper.CustomerDataMapper;
import kh.edu.istad.platform.customer.domain.port.out.CustomerSegmentRepository;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class InitiateUpdateCustomerSegmentUseCase {

    private final CustomerSegmentRepository customerSegmentRepository;
    private final CustomerDataMapper customerDataMapper;

    public Mono<CustomerSegmentResult> execute(UpdateCustomerSegmentCommand command) {
        log.info("Update customer segment: {}", command);

        if (command.id() == null || command.id().isBlank()) {
            return Mono.error(new DomainException("Segment ID must not be blank"));
        }
        if (command.name() == null || command.name().isBlank()) {
            return Mono.error(new DomainException("Segment name must not be blank"));
        }

        String segmentId = command.id().trim();
        String newName = command.name().trim();
        String description = (command.description() != null && !command.description().isBlank())
                ? command.description().trim()
                : newName + " Tier Customer";

        return customerSegmentRepository.findById(segmentId)
                .switchIfEmpty(customerSegmentRepository.findByNameIgnoreCase(segmentId))
                .switchIfEmpty(Mono.error(new DomainException("Customer segment not found with id: " + segmentId)))
                .flatMap(existing -> {
                    // Check if name is being changed
                    if (!existing.name().equalsIgnoreCase(newName)) {
                        // Business rule: Default system segment cannot be renamed (domain invariant)
                        existing.validateCanRename(newName);

                        return customerSegmentRepository.findByNameIgnoreCase(newName)
                                .flatMap(conflict -> {
                                    if (!conflict.id().equalsIgnoreCase(existing.id())) {
                                        return Mono.<CustomerSegment>error(new DomainException("Customer segment with name '" + newName + "' already exists"));
                                    }
                                    return Mono.just(existing);
                                })
                                .defaultIfEmpty(existing);
                    }
                    return Mono.just(existing);
                })
                .flatMap(existing -> {
                    CustomerSegment updated = new CustomerSegment(existing.id(), newName, description);
                    return customerSegmentRepository.save(updated);
                })
                .map(customerDataMapper::toCustomerSegmentResult);
    }
}
