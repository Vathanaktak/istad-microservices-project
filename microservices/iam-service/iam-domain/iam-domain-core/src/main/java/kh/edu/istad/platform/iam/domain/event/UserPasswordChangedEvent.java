package kh.edu.istad.platform.iam.domain.event;

import kh.edu.istad.platform.iam.domain.model.User;

import java.time.Instant;
import java.util.UUID;

/**
 * Fired when a {@link User} successfully changes or resets their password.
 *
 * <p>Consumers may use this to:
 * <ul>
 *   <li>Send a password-changed confirmation e-mail</li>
 *   <li>Invalidate all existing sessions / refresh tokens</li>
 * </ul>
 *
 * @param userUuid           UUID of the user
 * @param username           username of the user
 * @param passwordChangedAt  timestamp when the password was changed (from domain model)
 */
public record UserPasswordChangedEvent(
        UUID    userUuid,
        String  username,
        Instant passwordChangedAt
) implements DomainEvent<User> {

    public static UserPasswordChangedEvent of(User user) {
        return new UserPasswordChangedEvent(
                user.getUuid(),
                user.getUsername(),
                user.getPasswordChangedAt()
        );
    }
}
