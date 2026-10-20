package kh.edu.istad.platform.customer.domain.valueObject;

public record CustomerSegmentFilterCriteria(
        String id,
        String name,
        String description,
        String query
) {
    public CustomerSegmentFilterCriteria() {
        this(null, null, null, null);
    }

    public CustomerSegmentFilterCriteria(String name) {
        this(null, name, null, null);
    }

    public boolean isEmpty() {
        return (id == null || id.isBlank())
                && (name == null || name.isBlank())
                && (description == null || description.isBlank())
                && (query == null || query.isBlank());
    }
}
