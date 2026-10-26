package kh.edu.istad.platform.order.domain.dto;

import java.util.UUID;

public record GetCustomerResult(
        UUID customerId,
        String username,
        String familyName,
        String givenName,
        String email,
        String phoneNumber
) {
}
