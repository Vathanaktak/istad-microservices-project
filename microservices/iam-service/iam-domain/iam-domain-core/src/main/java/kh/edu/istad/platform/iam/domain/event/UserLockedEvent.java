package kh.edu.istad.platform.iam.domain.event;

import kh.edu.istad.platform.iam.domain.model.User;

import java.time.Instant;
import java.util.UUID;

/**
 * Fired when a {@link User} account is locked — either manually by an admin
 * or automatically after too many failed login attempts.
 *
 * <p>Consumers may use this to:
 * <ul>
 *   <li>Send a security alert e-mail to the user</li>
 *   <li>Invalidate active sessions / tokens</li>
 * </ul>
 *
 * @param userUuid         UUID of the locked user
 * @param username         username of the locked user
 * @param failedAttempts   number of failed login attempts at time of lock (0 if manually locked)
 * @param lockedAt         timestamp of the event
 */
public record UserLockedEvent(
        UUID    userUuid,
        String  username,
        int     failedAttempts,
        Instant lockedAt
) implements DomainEvent<User> {

    public static UserLockedEvent of(User user) {
        return new UserLockedEvent(
                user.getUuid(),
                user.getUsername(),
                user.getFailedLoginAttempts(),
                Instant.now()
        );
    }
}
