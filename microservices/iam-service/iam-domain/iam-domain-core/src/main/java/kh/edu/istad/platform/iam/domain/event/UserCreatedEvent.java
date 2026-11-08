package kh.edu.istad.platform.iam.domain.event;

import kh.edu.istad.platform.iam.domain.model.User;

import java.time.Instant;
import java.util.UUID;

/**
 * Fired when a new {@link User} account is registered or created by an admin.
 *
 * <p>Consumers may use this to:
 * <ul>
 *   <li>Send a welcome / verification e-mail</li>
 *   <li>Provision the user in downstream services (customer profile, etc.)</li>
 *   <li>Emit an audit log entry</li>
 * </ul>
 *
 * @param userUuid  UUID of the newly created user
 * @param username  chosen username
 * @param email     registered e-mail address
 * @param createdAt timestamp of the event
 */
public record UserCreatedEvent(
        UUID    userUuid,
        String  username,
        String  email,
        Instant createdAt
) implements DomainEvent<User> {

    public static UserCreatedEvent of(User user) {
        return new UserCreatedEvent(
                user.getUuid(),
                user.getUsername(),
                user.getEmail(),
                Instant.now()
        );
    }
}
