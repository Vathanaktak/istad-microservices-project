package kh.edu.istad.platform.iam.domain.event;

import kh.edu.istad.platform.iam.domain.model.Role;

import java.time.Instant;
import java.util.UUID;

/**
 * Fired when an existing {@link Role} is updated (name or description changed).
 *
 * @param roleUuid  UUID of the updated role
 * @param name      new role name
 * @param updatedAt timestamp of the event
 */
public record RoleUpdatedEvent(
        UUID    roleUuid,
        String  name,
        Instant updatedAt
) implements DomainEvent<Role> {

    public static RoleUpdatedEvent of(Role role) {
        return new RoleUpdatedEvent(
                role.getUuid(),
                role.getName(),
                Instant.now()
        );
    }
}
