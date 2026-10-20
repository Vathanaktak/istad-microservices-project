package kh.edu.istad.platform.customer.domain.dto;

public record UpdateCustomerSegmentCommand(
        String id,
        String name,
        String description
) {
}
