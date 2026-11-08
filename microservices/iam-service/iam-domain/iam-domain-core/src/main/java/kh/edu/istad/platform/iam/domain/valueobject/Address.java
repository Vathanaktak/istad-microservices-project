package kh.edu.istad.platform.iam.domain.valueobject;

/**
 * Postal address following the OIDC {@code address} claim structure.
 * {@code country} should be an ISO 3166-1 alpha-2 code (e.g. {@code "KH"}).
 * All fields are optional.
 */
public record Address(
        String streetAddress,
        String locality,
        String region,
        String postalCode,
        String country
) {

    public static Address empty() {
        return new Address(null, null, null, null, null);
    }

    public boolean isBlank() {
        return streetAddress == null && locality == null && region == null
                && postalCode == null && country == null;
    }
}
