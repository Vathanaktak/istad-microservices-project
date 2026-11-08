package kh.edu.istad.platform.iam.domain.event;

import kh.edu.istad.platform.iam.domain.model.Permission;

import java.time.Instant;
import java.util.UUID;

/**
 * Fired when a {@link Permission} is soft-deleted.
 *
 * <p>Consumers may use this to invalidate authority caches that reference this permission.
 *
 * @param permissionUuid UUID of the deleted permission
 * @param name           composite name of the deleted permission ({@code resource:action})
 * @param deletedBy      actor who performed the deletion
 * @param deletedAt      timestamp of the event
 */
public record PermissionDeletedEvent(
        UUID    permissionUuid,
        String  name,
        String  deletedBy,
        Instant deletedAt
) implements DomainEvent<Permission> {

    public static PermissionDeletedEvent of(Permission permission) {
        return new PermissionDeletedEvent(
                permission.getUuid(),
                permission.getName(),
                permission.getDeletedBy(),
                Instant.now()
        );
    }
}
