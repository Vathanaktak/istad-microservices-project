package kh.edu.istad.platform.customer.domain.dto;

import java.util.UUID;

public record GetCustomerResult(
        UUID customerId,
        String username,
        String givenName,
        String familyName,
        String email,
        String phoneNumber,
        String status
) {
}
