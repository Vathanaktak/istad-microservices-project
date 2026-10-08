package kh.edu.istad.platform.customer.restapi.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

public record CustomerUpdateInitiateResponse(
        UUID customerId,
        String familyName,
        String givenName,
        ZonedDateTime updatedAt
) {
}
