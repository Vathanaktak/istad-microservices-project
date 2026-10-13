package kh.edu.istad.platform.business.restapi.dto;

import jakarta.validation.constraints.NotBlank;

public record BusinessCreateRequest(
        @NotBlank(message = "legal name is required")
        String legalName,
        @NotBlank(message = "display name is is required")
        String displayName,
        @NotBlank(message = "email is is required")
        String email,
        @NotBlank(message = "phone Number is required")
        String phoneNumber
) {
}
