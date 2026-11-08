package kh.edu.istad.platform.iam.domain.service;

import kh.edu.istad.platform.iam.domain.event.PermissionCreatedEvent;
import kh.edu.istad.platform.iam.domain.event.PermissionDeletedEvent;
import kh.edu.istad.platform.iam.domain.event.PermissionUpdatedEvent;
import kh.edu.istad.platform.iam.domain.event.RoleCreatedEvent;
import kh.edu.istad.platform.iam.domain.event.RoleDeletedEvent;
import kh.edu.istad.platform.iam.domain.event.RoleUpdatedEvent;
import kh.edu.istad.platform.iam.domain.event.UserCreatedEvent;
import kh.edu.istad.platform.iam.domain.event.UserDeletedEvent;
import kh.edu.istad.platform.iam.domain.event.UserEnabledChangedEvent;
import kh.edu.istad.platform.iam.domain.event.UserLockedEvent;
import kh.edu.istad.platform.iam.domain.event.UserPasswordChangedEvent;
import kh.edu.istad.platform.iam.domain.event.UserUnlockedEvent;
import kh.edu.istad.platform.iam.domain.event.UserUpdatedEvent;
import kh.edu.istad.platform.iam.domain.model.Permission;
import kh.edu.istad.platform.iam.domain.model.Role;
import kh.edu.istad.platform.iam.domain.model.User;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Implementation of {@link IamDomainService}.
 *
 * <p>Follows the same two-step pattern as {@code CustomerDomainServiceImpl}:
 * <ol>
 *   <li>Call the aggregate's domain method — validation happens inside the aggregate.</li>
 *   <li>Build and return the domain event explicitly in the service, passing the
 *       timestamp from here (not hidden inside the event). </li>
 * </ol>
 */
public class IamDomainServiceImpl implements IamDomainService {

    // =========================================================================
    // User operations
    // =========================================================================

    @Override
    public UserCreatedEvent createUser(User user) {
        user.initiateUser();

        return new UserCreatedEvent(
                user.getUuid(),
                user.getUsername(),
                user.getEmail(),
                Instant.now()
        );
    }

    @Override
    public UserUpdatedEvent updateUser(User user, String givenName, String familyName,
                                       String middleName, String nickname,
                                       String gender, LocalDate birthdate, String locale,
                                       String profileUrl, String pictureUrl,
                                       String websiteUrl, String zoneinfo) {
        user.updateProfile(givenName, familyName, middleName, nickname,
                gender, birthdate, locale, profileUrl, pictureUrl, websiteUrl, zoneinfo);

        return new UserUpdatedEvent(
                user.getUuid(),
                user.getUsername(),
                user.getEmail(),
                Instant.now()
        );
    }

    @Override
    public UserPasswordChangedEvent changeUserPassword(User user, String newPasswordHash) {
        user.changePassword(newPasswordHash);

        return new UserPasswordChangedEvent(
                user.getUuid(),
                user.getUsername(),
                user.getPasswordChangedAt()
        );
    }

    @Override
    public UserLockedEvent lockUser(User user) {
        user.lockAccount();

        return new UserLockedEvent(
                user.getUuid(),
                user.getUsername(),
                user.getFailedLoginAttempts(),
                Instant.now()
        );
    }

    @Override
    public UserUnlockedEvent unlockUser(User user) {
        user.unlockAccount();

        return new UserUnlockedEvent(
                user.getUuid(),
                user.getUsername(),
                Instant.now()
        );
    }

    @Override
    public UserEnabledChangedEvent disableUser(User user) {
        user.disableUser();

        return new UserEnabledChangedEvent(
                user.getUuid(),
                user.getUsername(),
                user.isEnabled(),
                Instant.now()
        );
    }

    @Override
    public UserEnabledChangedEvent enableUser(User user) {
        user.enableUser();

        return new UserEnabledChangedEvent(
                user.getUuid(),
                user.getUsername(),
                user.isEnabled(),
                Instant.now()
        );
    }

    @Override
    public UserDeletedEvent deleteUser(User user, String actor) {
        user.deleteUser(actor);

        return new UserDeletedEvent(
                user.getUuid(),
                user.getUsername(),
                user.getDeletedBy(),
                Instant.now()
        );
    }

    // =========================================================================
    // Role operations
    // =========================================================================

    @Override
    public RoleCreatedEvent createRole(Role role) {
        role.initiateRole();

        return new RoleCreatedEvent(
                role.getUuid(),
                role.getName(),
                role.getDescription(),
                Instant.now()
        );
    }

    @Override
    public RoleUpdatedEvent updateRole(Role role, String newName, String description) {
        role.updateRole(newName, description);

        return new RoleUpdatedEvent(
                role.getUuid(),
                role.getName(),
                Instant.now()
        );
    }

    @Override
    public RoleDeletedEvent deleteRole(Role role, String actor) {
        role.deleteRole(actor);

        return new RoleDeletedEvent(
                role.getUuid(),
                role.getName(),
                role.getDeletedBy(),
                Instant.now()
        );
    }

    // =========================================================================
    // Permission operations
    // =========================================================================

    @Override
    public PermissionCreatedEvent createPermission(Permission permission) {
        permission.initiatePermission();

        return new PermissionCreatedEvent(
                permission.getUuid(),
                permission.getName(),
                permission.getResource(),
                permission.getAction(),
                Instant.now()
        );
    }

    @Override
    public PermissionUpdatedEvent updatePermission(Permission permission, String resource, String action, String description) {
        permission.updatePermission(resource, action, description);

        return new PermissionUpdatedEvent(
                permission.getUuid(),
                permission.getName(),
                Instant.now()
        );
    }

    @Override
    public PermissionDeletedEvent deletePermission(Permission permission, String actor) {
        permission.deletePermission(actor);

        return new PermissionDeletedEvent(
                permission.getUuid(),
                permission.getName(),
                permission.getDeletedBy(),
                Instant.now()
        );
    }
}
