package kh.edu.istad.platform.customer.domain.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

public record InitiateDeactivateCustomerResult(
        UUID customerId,
        ZonedDateTime deactivatedAt
) {
}
