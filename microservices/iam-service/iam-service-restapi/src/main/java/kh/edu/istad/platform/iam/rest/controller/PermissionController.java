package kh.edu.istad.platform.iam.rest.controller;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.usecase.*; import kh.edu.istad.platform.iam.application.result.PageResult;
import kh.edu.istad.platform.iam.rest.dto.*; import kh.edu.istad.platform.iam.rest.dto.Common.PageResponse; import kh.edu.istad.platform.iam.rest.dto.UserDtos.*; import kh.edu.istad.platform.iam.rest.dto.RoleDtos.*; import kh.edu.istad.platform.iam.rest.dto.PermissionDtos.*;
import jakarta.validation.Valid; import lombok.RequiredArgsConstructor; import java.util.*;
import org.springframework.http.HttpStatus; import org.springframework.http.ResponseEntity; import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal; import org.springframework.security.oauth2.jwt.Jwt; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/permissions") @RequiredArgsConstructor
public class PermissionController {
    private final CreatePermissionUseCase create;
    private final ListPermissionsUseCase list;
    private final GetPermissionUseCase get;
    private final UpdatePermissionUseCase update;
    private final DeletePermissionUseCase delete;
    private final ActivatePermissionUseCase activate;
    private final DeactivatePermissionUseCase deactivate;

    @PostMapping @PreAuthorize("hasAuthority('permission:create')")
    public ResponseEntity<PermissionResponse> create(@Valid @RequestBody CreatePermissionRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiMapper.permission(create.execute(new CreatePermissionCommand(r.resource(), r.action(), r.description()))));
    }
    @GetMapping @PreAuthorize("hasAuthority('permission:read')")
    public PageResponse<PermissionResponse> list(@RequestParam(required = false) String q, @RequestParam(required = false) String resource,
            @RequestParam(required = false) Boolean active, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String sort) {
        return PageResponse.of(list.execute(new PermissionFilterQuery(q, resource, active, page, size, sort)).map(ApiMapper::permission));
    }
    @GetMapping("/{uuid}") @PreAuthorize("hasAuthority('permission:read')")
    public PermissionResponse get(@PathVariable UUID uuid) { return ApiMapper.permission(get.execute(uuid)); }
    @PatchMapping("/{uuid}") @PreAuthorize("hasAuthority('permission:update')")
    public PermissionResponse update(@PathVariable UUID uuid, @Valid @RequestBody UpdatePermissionRequest r) {
        return ApiMapper.permission(update.execute(new UpdatePermissionCommand(uuid, r.description())));
    }
    @DeleteMapping("/{uuid}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAuthority('permission:delete')")
    public void delete(@PathVariable UUID uuid) { delete.execute(uuid); }
    @PostMapping("/{uuid}/activate") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAuthority('permission:update')")
    public void activate(@PathVariable UUID uuid) { activate.execute(uuid); }
    @PostMapping("/{uuid}/deactivate") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAuthority('permission:update')")
    public void deactivate(@PathVariable UUID uuid) { deactivate.execute(uuid); }
}
