package kh.edu.istad.platform.customer.restapi.dto;

import jakarta.validation.constraints.NotBlank;

public record CustomerUpdateInitiateRequest(
        @NotBlank
        String familyName,
        @NotBlank
        String givenName
) {
}
