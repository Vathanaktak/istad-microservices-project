package kh.edu.istad.platform.iam.domain.valueobject;

/**
 * E.164-format phone number, e.g. {@code +85512345678}.
 * The raw string is stored as-is; validation is enforced at the API layer.
 */
public record PhoneNumber(String value) {

    public static PhoneNumber of(String value) {
        return value == null ? null : new PhoneNumber(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
