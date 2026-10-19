package kh.edu.istad.platform.customer.domain.dto;

import java.util.UUID;

public record FindAllResult(
        UUID customerId,
        String username,
        String familyName,
        String givenName,
        String email,
        String phoneNumber,
        String customerSegment
) {
    public FindAllResult(UUID customerId, String username, String familyName, String givenName, String email, String phoneNumber) {
        this(customerId, username, familyName, givenName, email, phoneNumber, null);
    }
}
