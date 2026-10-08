package kh.edu.istad.platform.customer.domain.dto;

import java.util.UUID;

public record InitiateUpdateCustomerCommand(
        UUID customerId,
        String familyName,
        String givenName
) {
}
