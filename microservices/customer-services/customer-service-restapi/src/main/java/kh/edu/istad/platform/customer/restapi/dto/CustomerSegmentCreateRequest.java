package kh.edu.istad.platform.customer.restapi.dto;

import jakarta.validation.constraints.NotBlank;

public record CustomerSegmentCreateRequest(
        String id,
        @NotBlank(message = "Segment name is required")
        String name,
        String description
) {
    public CustomerSegmentCreateRequest(String name, String description) {
        this(null, name, description);
    }
}
