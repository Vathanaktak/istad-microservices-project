package kh.edu.istad.platform.iam.domain.event;

import kh.edu.istad.platform.iam.domain.model.User;

import java.time.Instant;
import java.util.UUID;

/**
 * Fired when a {@link User}'s profile information is updated
 * (e.g. name, phone number, address, locale).
 *
 * <p>Consumers may use this to sync profile data to downstream services.
 *
 * @param userUuid  UUID of the updated user
 * @param username  current username (unchanged)
 * @param email     current e-mail (unchanged)
 * @param updatedAt timestamp of the event
 */
public record UserUpdatedEvent(
        UUID    userUuid,
        String  username,
        String  email,
        Instant updatedAt
) implements DomainEvent<User> {

    public static UserUpdatedEvent of(User user) {
        return new UserUpdatedEvent(
                user.getUuid(),
                user.getUsername(),
                user.getEmail(),
                Instant.now()
        );
    }
}
