package kh.edu.istad.platform.iam.domain.event;

import kh.edu.istad.platform.iam.domain.model.Permission;

import java.time.Instant;
import java.util.UUID;

/**
 * Fired when an existing {@link Permission} is updated (description changed).
 *
 * @param permissionUuid UUID of the updated permission
 * @param name           composite permission name ({@code resource:action})
 * @param updatedAt      timestamp of the event
 */
public record PermissionUpdatedEvent(
        UUID    permissionUuid,
        String  name,
        Instant updatedAt
) implements DomainEvent<Permission> {

    public static PermissionUpdatedEvent of(Permission permission) {
        return new PermissionUpdatedEvent(
                permission.getUuid(),
                permission.getName(),
                Instant.now()
        );
    }
}
