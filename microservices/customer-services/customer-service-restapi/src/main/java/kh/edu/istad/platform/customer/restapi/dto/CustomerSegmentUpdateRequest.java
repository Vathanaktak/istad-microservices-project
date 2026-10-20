package kh.edu.istad.platform.customer.restapi.dto;

import jakarta.validation.constraints.NotBlank;

public record CustomerSegmentUpdateRequest(
        @NotBlank(message = "Segment name is required")
        String name,
        String description
) {
}
