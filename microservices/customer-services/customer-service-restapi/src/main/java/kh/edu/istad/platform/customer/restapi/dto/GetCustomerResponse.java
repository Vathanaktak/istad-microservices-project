package kh.edu.istad.platform.customer.restapi.dto;

import java.util.UUID;

public record GetCustomerResponse(
            UUID customerId,
            String username,
            String givenName,
            String familyName,
            String email,
            String phoneNumber,
            String status
) {
}
