package kh.edu.istad.platform.iam.domain.valueobject;

import java.util.Locale;

/**
 * Normalised (lower-cased) e-mail address.
 */
public record Email(String value) {

    public Email {
        if (value != null) value = value.trim().toLowerCase(Locale.ROOT);
    }

    public static Email of(String value) {
        return value == null ? null : new Email(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
