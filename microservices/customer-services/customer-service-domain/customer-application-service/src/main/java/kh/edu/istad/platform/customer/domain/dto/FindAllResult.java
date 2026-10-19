package kh.edu.istad.platform.customer.domain.dto;

import java.util.UUID;

public record FindAllResult(
        UUID customerId,
        String username,
        String familyName,
        String givenName,
        String email,
        String phoneNumber
) {
    public FindAllResult(String username, String familyName, String givenName, String email, String phoneNumber) {
        this(null, username, familyName, givenName, email, phoneNumber);
    }
}
