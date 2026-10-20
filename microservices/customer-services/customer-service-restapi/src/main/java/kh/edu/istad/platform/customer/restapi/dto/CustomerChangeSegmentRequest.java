package kh.edu.istad.platform.customer.restapi.dto;

import jakarta.validation.constraints.NotBlank;

public record CustomerChangeSegmentRequest(
        @NotBlank(message = "Segment ID is required")
        String segmentId
) {
}
