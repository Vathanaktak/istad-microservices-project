package kh.edu.istad.platform.iam.rest.controller;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.usecase.*; import kh.edu.istad.platform.iam.application.result.PageResult;
import kh.edu.istad.platform.iam.rest.dto.*; import kh.edu.istad.platform.iam.rest.dto.Common.PageResponse; import kh.edu.istad.platform.iam.rest.dto.UserDtos.*; import kh.edu.istad.platform.iam.rest.dto.RoleDtos.*; import kh.edu.istad.platform.iam.rest.dto.PermissionDtos.*;
import jakarta.validation.Valid; import lombok.RequiredArgsConstructor; import java.util.*;
import org.springframework.http.HttpStatus; import org.springframework.http.ResponseEntity; import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal; import org.springframework.security.oauth2.jwt.Jwt; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/users") @RequiredArgsConstructor
public class UserController {
    private final CreateUserUseCase create;
    private final ListUsersUseCase list;
    private final GetUserUseCase get;
    private final UpdateUserProfileUseCase update;
    private final DeleteUserUseCase delete;
    private final EnableUserUseCase enable;
    private final DisableUserUseCase disable;
    private final LockUserUseCase lock;
    private final UnlockUserUseCase unlock;
    private final ResetUserPasswordUseCase resetPassword;
    private final GetUserAuthoritiesUseCase authorities;
    private final AssignRolesToUserUseCase assignRoles;
    private final RevokeRoleFromUserUseCase revokeRole;
    private final GrantPermissionsToUserUseCase grantPermissions;
    private final RevokePermissionFromUserUseCase revokePermission;
    private final ListUserIdentitiesUseCase identities;
    private final UnlinkIdentityUseCase unlinkIdentity;

    @PostMapping @PreAuthorize("hasAuthority('user:create')")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest r) {
        var res = create.execute(new CreateUserCommand(r.username(), r.email(), r.password(), r.givenName(), r.familyName(), r.gender(),
            r.birthdate(), r.phoneNumber(), r.zoneinfo(), r.locale(), ApiMapper.address(r.address()), r.roleUuids()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiMapper.user(res));
    }

    @GetMapping @PreAuthorize("hasAuthority('user:read')")
    public PageResponse<UserSummaryResponse> list(@RequestParam(required = false) String q, @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String sort) {
        return PageResponse.of(list.execute(new UserFilterQuery(q, enabled, page, size, sort)).map(ApiMapper::summary));
    }

    @GetMapping("/{uuid}") @PreAuthorize("hasAuthority('user:read')")
    public UserResponse get(@PathVariable UUID uuid) { return ApiMapper.user(get.execute(uuid)); }

    @PatchMapping("/{uuid}") @PreAuthorize("hasAuthority('user:update')")
    public UserResponse update(@PathVariable UUID uuid, @Valid @RequestBody UpdateUserProfileRequest r) {
        return ApiMapper.user(update.execute(MeController.toCommand(uuid, r)));
    }

    @DeleteMapping("/{uuid}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAuthority('user:delete')")
    public void delete(@PathVariable UUID uuid) { delete.execute(uuid); }

    @PostMapping("/{uuid}/enable") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAuthority('user:update')")
    public void enable(@PathVariable UUID uuid) { enable.execute(uuid); }
    @PostMapping("/{uuid}/disable") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAuthority('user:update')")
    public void disable(@PathVariable UUID uuid) { disable.execute(uuid); }
    @PostMapping("/{uuid}/lock") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAuthority('user:update')")
    public void lock(@PathVariable UUID uuid) { lock.execute(uuid); }
    @PostMapping("/{uuid}/unlock") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAuthority('user:update')")
    public void unlock(@PathVariable UUID uuid) { unlock.execute(uuid); }

    @PutMapping("/{uuid}/password") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAuthority('user:update')")
    public void resetPassword(@PathVariable UUID uuid, @Valid @RequestBody ResetPasswordRequest r) {
        resetPassword.execute(new ResetUserPasswordCommand(uuid, r.newPassword()));
    }

    @GetMapping("/{uuid}/authorities") @PreAuthorize("hasAuthority('user:read')")
    public UserAuthoritiesResponse authorities(@PathVariable UUID uuid) { return ApiMapper.authorities(authorities.execute(uuid)); }

    @PostMapping("/{uuid}/roles") @PreAuthorize("hasAuthority('user:assign')")
    public ResponseEntity<List<RoleAssignmentResponse>> assignRoles(@PathVariable UUID uuid, @Valid @RequestBody AssignRolesToUserRequest r) {
        var res = assignRoles.execute(new AssignRolesToUserCommand(uuid, r.roleUuids(), r.expiresAt(), r.reason()));
        return ResponseEntity.status(HttpStatus.CREATED).body(res.stream().map(ApiMapper::roleAssignment).toList());
    }
    @DeleteMapping("/{uuid}/roles/{roleUuid}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAuthority('user:assign')")
    public void revokeRole(@PathVariable UUID uuid, @PathVariable UUID roleUuid, @RequestParam(required = false) String reason) {
        revokeRole.execute(uuid, roleUuid, reason);
    }

    @PostMapping("/{uuid}/permissions") @PreAuthorize("hasAuthority('user:assign')")
    public ResponseEntity<List<PermissionGrantResponse>> grant(@PathVariable UUID uuid, @Valid @RequestBody GrantPermissionsToUserRequest r) {
        var res = grantPermissions.execute(new GrantPermissionsToUserCommand(uuid, r.permissionUuids(), r.effect(), r.expiresAt(), r.reason()));
        return ResponseEntity.status(HttpStatus.CREATED).body(res.stream().map(ApiMapper::grant).toList());
    }
    @DeleteMapping("/{uuid}/permissions/{permissionUuid}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAuthority('user:assign')")
    public void revokePermission(@PathVariable UUID uuid, @PathVariable UUID permissionUuid, @RequestParam(required = false) String reason) {
        revokePermission.execute(uuid, permissionUuid, reason);
    }

    @GetMapping("/{uuid}/identities") @PreAuthorize("hasAuthority('user:read')")
    public List<IdentityResponse> identities(@PathVariable UUID uuid) { return identities.execute(uuid).stream().map(ApiMapper::identity).toList(); }
    @DeleteMapping("/{uuid}/identities/{identityUuid}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAuthority('user:update')")
    public void unlink(@PathVariable UUID uuid, @PathVariable UUID identityUuid) { unlinkIdentity.execute(uuid, identityUuid); }
}
