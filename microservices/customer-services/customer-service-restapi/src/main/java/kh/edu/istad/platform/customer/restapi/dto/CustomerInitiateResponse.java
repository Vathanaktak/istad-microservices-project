package kh.edu.istad.platform.customer.restapi.dto;

import java.util.UUID;

public record CustomerInitiateResponse(
        UUID customerId,
        String username,
        String familyName,
        String givenName,
        String email,
        String phoneNumber,
        String customerSegment
) {
    public CustomerInitiateResponse(UUID customerId, String username, String familyName, String givenName, String email, String phoneNumber) {
        this(customerId, username, familyName, givenName, email, phoneNumber, null);
    }
}
