package kh.edu.istad.platform.customer.domain.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

public record InitiateUpdateCustomerResult(
        UUID customerId,
        String familyName,
        String givenName,
        ZonedDateTime updatedAt
) {}