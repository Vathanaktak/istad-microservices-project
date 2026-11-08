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
import java.time.LocalDate;

/**
 * Domain service for IAM aggregate lifecycle operations.
 *
 * <p>Each method delegates business-rule validation to the aggregate model,
 * then returns the corresponding domain event — mirroring the pattern used
 * in {@code CustomerDomainService}.
 */
public interface IamDomainService {

    // -------------------------------------------------------------------------
    // User operations
    // -------------------------------------------------------------------------

    /** Validates and initialises a new user, then returns a {@link UserCreatedEvent}. */
    UserCreatedEvent createUser(User user);

    /** Applies a profile update to the user, then returns a {@link UserUpdatedEvent}. */
    UserUpdatedEvent updateUser(User user, String givenName, String familyName,
                                String middleName, String nickname,
                                String gender, LocalDate birthdate, String locale,
                                String profileUrl, String pictureUrl,
                                String websiteUrl, String zoneinfo);

    /** Changes the user's password hash, then returns a {@link UserPasswordChangedEvent}. */
    UserPasswordChangedEvent changeUserPassword(User user, String newPasswordHash);

    /** Locks the user account, then returns a {@link UserLockedEvent}. */
    UserLockedEvent lockUser(User user);

    /** Unlocks the user account, then returns a {@link UserUnlockedEvent}. */
    UserUnlockedEvent unlockUser(User user);

    /** Disables the user account, then returns a {@link UserEnabledChangedEvent}. */
    UserEnabledChangedEvent disableUser(User user);

    /** Re-enables the user account, then returns a {@link UserEnabledChangedEvent}. */
    UserEnabledChangedEvent enableUser(User user);

    /** Soft-deletes the user account, then returns a {@link UserDeletedEvent}. */
    UserDeletedEvent deleteUser(User user, String actor);

    // -------------------------------------------------------------------------
    // Role operations
    // -------------------------------------------------------------------------

    /** Validates and initialises a new role, then returns a {@link RoleCreatedEvent}. */
    RoleCreatedEvent createRole(Role role);

    /** Updates the role's name and/or description, then returns a {@link RoleUpdatedEvent}. */
    RoleUpdatedEvent updateRole(Role role, String newName, String description);

    /** Soft-deletes the role, then returns a {@link RoleDeletedEvent}. */
    RoleDeletedEvent deleteRole(Role role, String actor);

    // -------------------------------------------------------------------------
    // Permission operations
    // -------------------------------------------------------------------------

    /** Validates and initialises a new permission, then returns a {@link PermissionCreatedEvent}. */
    PermissionCreatedEvent createPermission(Permission permission);

    /** Updates the permission's resource, action, and/or description, then returns a {@link PermissionUpdatedEvent}. */
    PermissionUpdatedEvent updatePermission(Permission permission, String resource, String action, String description);

    /** Soft-deletes the permission, then returns a {@link PermissionDeletedEvent}. */
    PermissionDeletedEvent deletePermission(Permission permission, String actor);
}
