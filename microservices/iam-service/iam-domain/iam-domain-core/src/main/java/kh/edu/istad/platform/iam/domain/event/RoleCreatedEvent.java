package kh.edu.istad.platform.iam.domain.event;

import kh.edu.istad.platform.iam.domain.model.Role;

import java.time.Instant;
import java.util.UUID;

/**
 * Fired when a new {@link Role} is created.
 *
 * <p>Consumers may use this to replicate role definitions to other services
 * or update permission caches.
 *
 * @param roleUuid    UUID of the new role
 * @param name        role name (e.g. {@code "ADMIN"})
 * @param description optional human-readable description
 * @param createdAt   timestamp of the event
 */
public record RoleCreatedEvent(
        UUID    roleUuid,
        String  name,
        String  description,
        Instant createdAt
) implements DomainEvent<Role> {

    public static RoleCreatedEvent of(Role role) {
        return new RoleCreatedEvent(
                role.getUuid(),
                role.getName(),
                role.getDescription(),
                Instant.now()
        );
    }
}
