package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.common.exception.DomainException;
import kh.edu.istad.platform.customer.domain.dto.CreateCustomerSegmentCommand;
import kh.edu.istad.platform.customer.domain.dto.CustomerSegmentResult;
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
public class InitiateCreateCustomerSegmentUseCase {

    private final CustomerSegmentRepository customerSegmentRepository;
    private final CustomerDataMapper customerDataMapper;

    public Mono<CustomerSegmentResult> execute(CreateCustomerSegmentCommand command) {
        log.info("Create customer segment: {}", command);

        if (command.name() == null || command.name().isBlank()) {
            return Mono.error(new DomainException("Customer segment name must not be blank"));
        }

        String segmentName = command.name().trim();
        String segmentId = (command.id() != null && !command.id().isBlank())
                ? command.id().trim().toUpperCase()
                : CustomerSegment.generateId(segmentName);

        String description = (command.description() != null && !command.description().isBlank())
                ? command.description().trim()
                : segmentName + " Tier Customer";

        return customerSegmentRepository.existsByNameIgnoreCase(segmentName)
                .flatMap(nameExists -> {
                    if (Boolean.TRUE.equals(nameExists)) {
                        return Mono.error(new DomainException("Customer segment with name '" + segmentName + "' already exists"));
                    }
                    return customerSegmentRepository.existsById(segmentId);
                })
                .flatMap(idExists -> {
                    if (Boolean.TRUE.equals(idExists)) {
                        return Mono.error(new DomainException("Customer segment with ID '" + segmentId + "' already exists"));
                    }
                    CustomerSegment segment = new CustomerSegment(segmentId, segmentName, description);
                    return customerSegmentRepository.save(segment);
                })
                .map(customerDataMapper::toCustomerSegmentResult);
    }
}
