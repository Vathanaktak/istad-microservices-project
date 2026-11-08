package kh.edu.istad.platform.iam.domain.event;

import kh.edu.istad.platform.iam.domain.model.User;

import java.time.Instant;
import java.util.UUID;

/**
 * Fired when a {@link User} account is enabled or disabled by an admin.
 *
 * @param userUuid  UUID of the affected user
 * @param username  username of the affected user
 * @param enabled   {@code true} if the account was enabled, {@code false} if disabled
 * @param changedAt timestamp of the event
 */
public record UserEnabledChangedEvent(
        UUID    userUuid,
        String  username,
        boolean enabled,
        Instant changedAt
) implements DomainEvent<User> {

    public static UserEnabledChangedEvent of(User user) {
        return new UserEnabledChangedEvent(
                user.getUuid(),
                user.getUsername(),
                user.isEnabled(),
                Instant.now()
        );
    }
}
