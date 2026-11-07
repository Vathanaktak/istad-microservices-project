package kh.edu.istad.platform.iam.rest.controller;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.usecase.*; import kh.edu.istad.platform.iam.application.result.PageResult;
import kh.edu.istad.platform.iam.rest.dto.*; import kh.edu.istad.platform.iam.rest.dto.Common.PageResponse; import kh.edu.istad.platform.iam.rest.dto.UserDtos.*; import kh.edu.istad.platform.iam.rest.dto.RoleDtos.*; import kh.edu.istad.platform.iam.rest.dto.PermissionDtos.*;
import jakarta.validation.Valid; import lombok.RequiredArgsConstructor; import java.util.*;
import org.springframework.http.HttpStatus; import org.springframework.http.ResponseEntity; import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal; import org.springframework.security.oauth2.jwt.Jwt; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/roles") @RequiredArgsConstructor
public class RoleController {
    private final CreateRoleUseCase create;
    private final ListRolesUseCase list;
    private final GetRoleUseCase get;
    private final UpdateRoleUseCase update;
    private final DeleteRoleUseCase delete;
    private final ActivateRoleUseCase activate;
    private final DeactivateRoleUseCase deactivate;
    private final AssignPermissionsToRoleUseCase assign;
    private final RevokePermissionFromRoleUseCase revoke;

    @PostMapping @PreAuthorize("hasAuthority('role:create')")
    public ResponseEntity<RoleResponse> create(@Valid @RequestBody CreateRoleRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiMapper.role(create.execute(new CreateRoleCommand(r.name(), r.description(), r.permissionUuids()))));
    }
    @GetMapping @PreAuthorize("hasAuthority('role:read')")
    public PageResponse<RoleSummaryResponse> list(@RequestParam(required = false) String q, @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String sort) {
        return PageResponse.of(list.execute(new RoleFilterQuery(q, active, page, size, sort)).map(ApiMapper::roleSummary));
    }
    @GetMapping("/{uuid}") @PreAuthorize("hasAuthority('role:read')")
    public RoleResponse get(@PathVariable UUID uuid) { return ApiMapper.role(get.execute(uuid)); }
    @PatchMapping("/{uuid}") @PreAuthorize("hasAuthority('role:update')")
    public RoleResponse update(@PathVariable UUID uuid, @Valid @RequestBody UpdateRoleRequest r) {
        return ApiMapper.role(update.execute(new UpdateRoleCommand(uuid, r.name(), r.description())));
    }
    @DeleteMapping("/{uuid}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAuthority('role:delete')")
    public void delete(@PathVariable UUID uuid) { delete.execute(uuid); }
    @PostMapping("/{uuid}/activate") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAuthority('role:update')")
    public void activate(@PathVariable UUID uuid) { activate.execute(uuid); }
    @PostMapping("/{uuid}/deactivate") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAuthority('role:update')")
    public void deactivate(@PathVariable UUID uuid) { deactivate.execute(uuid); }

    @PostMapping("/{uuid}/permissions") @PreAuthorize("hasAuthority('role:assign')")
    public ResponseEntity<List<PermissionAssignmentResponse>> assign(@PathVariable UUID uuid, @Valid @RequestBody AssignPermissionsToRoleRequest r) {
        var res = assign.execute(new AssignPermissionsToRoleCommand(uuid, r.permissionUuids(), r.expiresAt(), r.reason()));
        return ResponseEntity.status(HttpStatus.CREATED).body(res.stream().map(ApiMapper::permissionAssignment).toList());
    }
    @DeleteMapping("/{uuid}/permissions/{permissionUuid}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAuthority('role:assign')")
    public void revoke(@PathVariable UUID uuid, @PathVariable UUID permissionUuid, @RequestParam(required = false) String reason) {
        revoke.execute(uuid, permissionUuid, reason);
    }
}
