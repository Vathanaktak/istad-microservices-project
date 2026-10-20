package kh.edu.istad.platform.customer.domain.dto;

import java.util.UUID;

public record ChangeCustomerSegmentCommand(
        UUID customerId,
        String segmentId
) {
}
