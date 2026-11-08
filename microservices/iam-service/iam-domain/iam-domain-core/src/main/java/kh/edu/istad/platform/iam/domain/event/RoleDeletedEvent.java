package kh.edu.istad.platform.iam.domain.event;

import kh.edu.istad.platform.iam.domain.model.Role;

import java.time.Instant;
import java.util.UUID;

/**
 * Fired when a {@link Role} is soft-deleted.
 *
 * <p>Consumers may use this to invalidate authority caches that include this role.
 *
 * @param roleUuid  UUID of the deleted role
 * @param name      name of the deleted role
 * @param deletedBy actor who performed the deletion
 * @param deletedAt timestamp of the event
 */
public record RoleDeletedEvent(
        UUID    roleUuid,
        String  name,
        String  deletedBy,
        Instant deletedAt
) implements DomainEvent<Role> {

    public static RoleDeletedEvent of(Role role) {
        return new RoleDeletedEvent(
                role.getUuid(),
                role.getName(),
                role.getDeletedBy(),
                Instant.now()
        );
    }
}
