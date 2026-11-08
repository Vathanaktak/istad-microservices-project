package kh.edu.istad.platform.iam.domain.event;

import kh.edu.istad.platform.iam.domain.model.Permission;

import java.time.Instant;
import java.util.UUID;

/**
 * Fired when a new {@link Permission} is created.
 *
 * <p>Permission names follow the {@code resource:action} convention, e.g. {@code "user:read"}.
 *
 * @param permissionUuid UUID of the new permission
 * @param name           composite permission name ({@code resource:action})
 * @param resource       resource part of the permission
 * @param action         action part of the permission
 * @param createdAt      timestamp of the event
 */
public record PermissionCreatedEvent(
        UUID    permissionUuid,
        String  name,
        String  resource,
        String  action,
        Instant createdAt
) implements DomainEvent<Permission> {

    public static PermissionCreatedEvent of(Permission permission) {
        return new PermissionCreatedEvent(
                permission.getUuid(),
                permission.getName(),
                permission.getResource(),
                permission.getAction(),
                Instant.now()
        );
    }
}
