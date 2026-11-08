package kh.edu.istad.platform.iam.domain.valueobject;

/**
 * Role name used as a Spring Security authority prefix, e.g. {@code "ADMIN"}.
 * The authority string is {@code "ROLE_" + value}.
 */
public record RoleName(String value) {

    public static RoleName of(String value) {
        return value == null ? null : new RoleName(value);
    }

    /** Returns the full Spring Security authority string. */
    public String authority() {
        return "ROLE_" + value;
    }

    @Override
    public String toString() {
        return value;
    }
}
