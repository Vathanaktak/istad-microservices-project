package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.common.exception.DomainException;
import kh.edu.istad.platform.customer.domain.port.out.CustomerSegmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class InitiateDeleteCustomerSegmentUseCase {

    private final CustomerSegmentRepository customerSegmentRepository;

    public Mono<Boolean> deleteById(String id) {
        log.info("Delete customer segment with id: {}", id);

        if (id == null || id.isBlank()) {
            return Mono.error(new DomainException("Segment ID must not be blank"));
        }

        String segmentId = id.trim();

        return customerSegmentRepository.findById(segmentId)
                .switchIfEmpty(customerSegmentRepository.findByNameIgnoreCase(segmentId))
                .switchIfEmpty(Mono.error(new DomainException("Customer segment not found with id: " + segmentId)))
                .flatMap(segment -> {
                    // Business rule 1: Default system segment cannot be deleted (domain invariant)
                    segment.validateCanDelete();

                    // Business rule 2: In-use segment cannot be deleted
                    return customerSegmentRepository.isSegmentInUse(segment.id(), segment.name())
                            .flatMap(inUse -> {
                                if (Boolean.TRUE.equals(inUse)) {
                                    return Mono.error(new DomainException(
                                            "Cannot delete customer segment '" + segment.name() + "' because customers are currently assigned to it"
                                    ));
                                }
                                return customerSegmentRepository.deleteById(segment.id());
                            });
                });
    }
}
