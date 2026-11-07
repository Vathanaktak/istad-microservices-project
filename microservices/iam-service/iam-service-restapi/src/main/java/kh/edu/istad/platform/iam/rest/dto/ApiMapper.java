package kh.edu.istad.platform.iam.rest.dto;

import static kh.edu.istad.platform.iam.rest.dto.RoleDtos.*;
import static kh.edu.istad.platform.iam.rest.dto.UserDtos.*;

import kh.edu.istad.platform.iam.application.command.AddressCommand;
import kh.edu.istad.platform.iam.application.result.*;

public final class ApiMapper {
    private ApiMapper() {}

    public static AddressCommand address(AddressDto a) {
        return a == null ? null : new AddressCommand(a.streetAddress(), a.locality(), a.region(), a.postalCode(), a.country());
    }
    public static UserResponse user(UserResult u) {
        return new UserResponse(u.uuid(), u.username(), u.email(), u.emailVerified(), u.name(), u.givenName(), u.familyName(), u.locale(),
            u.enabled(), u.roles().stream().map(r -> new RoleRefResponse(r.uuid(), r.name())).toList(), u.createdAt(), u.updatedAt());
    }
    public static UserSummaryResponse summary(UserSummary u) {
        return new UserSummaryResponse(u.uuid(), u.username(), u.email(), u.name(), u.enabled(), !u.accountNonLocked());
    }
    public static RoleAssignmentResponse roleAssignment(RoleAssignmentResult r) {
        return new RoleAssignmentResponse(r.roleUuid(), r.roleName(), r.assignedAt(), r.expiresAt(), r.reason());
    }
    public static PermissionGrantResponse grant(PermissionGrantResult g) {
        return new PermissionGrantResponse(g.permissionUuid(), g.name(), g.effect(), g.expiresAt());
    }
    public static UserAuthoritiesResponse authorities(UserAuthoritiesResult a) {
        return new UserAuthoritiesResponse(a.roles().stream().map(ApiMapper::roleAssignment).toList(),
            a.directPermissions().stream().map(ApiMapper::grant).toList(), a.effectivePermissions());
    }
    public static IdentityResponse identity(IdentityResult i) {
        return new IdentityResponse(i.uuid(), i.provider(), i.email(), i.displayName(), i.linkedAt());
    }
    public static RoleSummaryResponse roleSummary(RoleSummary r) {
        return new RoleSummaryResponse(r.uuid(), r.name(), r.description(), r.system(), r.active());
    }
    public static PermissionResponse permission(PermissionResult p) {
        return new PermissionResponse(p.uuid(), p.name(), p.resource(), p.action(), p.description(), p.system(), p.active());
    }
    public static RoleResponse role(RoleResult r) {
        return new RoleResponse(r.uuid(), r.name(), r.description(), r.system(), r.active(),
            r.permissions().stream().map(ApiMapper::permission).toList(), r.createdAt(), r.updatedAt());
    }
    public static PermissionAssignmentResponse permissionAssignment(PermissionAssignmentResult a) {
        return new PermissionAssignmentResponse(a.permissionUuid(), a.name(), a.assignedAt(), a.expiresAt());
    }
}
