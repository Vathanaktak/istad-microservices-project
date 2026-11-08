package kh.edu.istad.platform.iam.domain.valueobject;

/**
 * Trimmed username (letters, digits, '.', '_', '-').
 */
public record Username(String value) {

    public Username {
        if (value != null) value = value.trim();
    }

    public static Username of(String value) {
        return value == null ? null : new Username(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
