package kh.edu.istad.platform.order.domain.valueobject;

public record StreetAddress(
        String street,
        String postalCode,
        String city
) {
}
