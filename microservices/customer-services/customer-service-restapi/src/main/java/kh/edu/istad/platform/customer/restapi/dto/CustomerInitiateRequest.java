package kh.edu.istad.platform.customer.restapi.dto;

import jakarta.validation.constraints.NotBlank;

public record CustomerInitiateRequest(
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank
        String familyName,
        @NotBlank
        String givenName,
        @NotBlank
        String email,
        @NotBlank
        String phoneNumber,
        String customerSegment
) {
    public CustomerInitiateRequest(String username, String familyName, String givenName, String email, String phoneNumber) {
        this(username, familyName, givenName, email, phoneNumber, null);
    }
}
