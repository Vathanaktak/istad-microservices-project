package kh.edu.istad.platform.customer.domain.valueObject;

public record CustomerFilterCriteria(
        String name,
        String email,
        String phoneNumber,
        String status,
        String customerSegment
) {
    public CustomerFilterCriteria(String name, String email, String phoneNumber, String status) {
        this(name, email, phoneNumber, status, null);
    }
}
