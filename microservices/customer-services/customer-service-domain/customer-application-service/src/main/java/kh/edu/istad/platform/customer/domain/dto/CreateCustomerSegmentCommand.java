package kh.edu.istad.platform.customer.domain.dto;

public record CreateCustomerSegmentCommand(
        String id,
        String name,
        String description
) {
    public CreateCustomerSegmentCommand(String name, String description) {
        this(null, name, description);
    }
}
