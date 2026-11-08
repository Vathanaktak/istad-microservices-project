package kh.edu.istad.platform.iam.domain.event;

import kh.edu.istad.platform.iam.domain.model.User;

import java.time.Instant;
import java.util.UUID;

/**
 * Fired when a previously locked {@link User} account is unlocked by an admin.
 *
 * <p>Consumers may use this to notify the user that their account has been restored.
 *
 * @param userUuid    UUID of the unlocked user
 * @param username    username of the unlocked user
 * @param unlockedAt  timestamp of the event
 */
public record UserUnlockedEvent(
        UUID    userUuid,
        String  username,
        Instant unlockedAt
) implements DomainEvent<User> {

    public static UserUnlockedEvent of(User user) {
        return new UserUnlockedEvent(
                user.getUuid(),
                user.getUsername(),
                Instant.now()
        );
    }
}
