package kh.edu.istad.platform.iam.domain.valueobject;

import java.util.Locale;

/**
 * Composite permission name in the form {@code resource:action}, e.g. {@code "user:read"}.
 */
public record PermissionName(String value) {

    /** Build a {@link PermissionName} from separate resource and action strings. */
    public static PermissionName of(String resource, String action) {
        return new PermissionName(
                resource.trim().toLowerCase(Locale.ROOT) + ":" + action.trim().toLowerCase(Locale.ROOT)
        );
    }

    public static PermissionName of(String value) {
        return value == null ? null : new PermissionName(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
