package kh.edu.istad.platform.iam.domain.event;

import kh.edu.istad.platform.iam.domain.model.User;

import java.time.Instant;
import java.util.UUID;

/**
 * Fired when a {@link User} account is soft-deleted.
 *
 * <p>Consumers may use this to:
 * <ul>
 *   <li>Invalidate active sessions / access tokens for the user</li>
 *   <li>Archive the user in downstream services</li>
 * </ul>
 *
 * @param userUuid  UUID of the deleted user
 * @param username  username at time of deletion
 * @param deletedBy actor (UUID string or "system") who performed the deletion
 * @param deletedAt timestamp of the event
 */
public record UserDeletedEvent(
        UUID    userUuid,
        String  username,
        String  deletedBy,
        Instant deletedAt
) implements DomainEvent<User> {

    public static UserDeletedEvent of(User user) {
        return new UserDeletedEvent(
                user.getUuid(),
                user.getUsername(),
                user.getDeletedBy(),
                Instant.now()
        );
    }
}
