package kh.edu.istad.platform.iam.rest.controller;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.usecase.*; import kh.edu.istad.platform.iam.application.result.PageResult;
import kh.edu.istad.platform.iam.rest.dto.*; import kh.edu.istad.platform.iam.rest.dto.Common.PageResponse; import kh.edu.istad.platform.iam.rest.dto.UserDtos.*; import kh.edu.istad.platform.iam.rest.dto.RoleDtos.*; import kh.edu.istad.platform.iam.rest.dto.PermissionDtos.*;
import jakarta.validation.Valid; import lombok.RequiredArgsConstructor; import java.util.*;
import org.springframework.http.HttpStatus; import org.springframework.http.ResponseEntity; import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal; import org.springframework.security.oauth2.jwt.Jwt; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/me") @RequiredArgsConstructor
public class MeController {
    private final GetUserUseCase get;
    private final UpdateUserProfileUseCase update;
    private final ChangePasswordUseCase changePassword;
    private final ListUserIdentitiesUseCase identities;
    private final UnlinkIdentityUseCase unlinkIdentity;

    static UpdateUserProfileCommand toCommand(UUID uuid, UpdateUserProfileRequest r) {
        return new UpdateUserProfileCommand(uuid, r.givenName(), r.familyName(), r.middleName(), r.nickname(), r.gender(), r.birthdate(),
            r.phoneNumber(), r.profileUrl(), r.pictureUrl(), r.websiteUrl(), r.zoneinfo(), r.locale(), ApiMapper.address(r.address()));
    }
    private static UUID me(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }

    @GetMapping
    public UserResponse get(@AuthenticationPrincipal Jwt jwt) { return ApiMapper.user(get.execute(me(jwt))); }

    @PatchMapping
    public UserResponse update(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateUserProfileRequest r) {
        return ApiMapper.user(update.execute(toCommand(me(jwt), r)));
    }

    @PutMapping("/password") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void password(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ChangePasswordRequest r) {
        changePassword.execute(new ChangePasswordCommand(me(jwt), r.currentPassword(), r.newPassword()));
    }

    @GetMapping("/identities")
    public List<IdentityResponse> identities(@AuthenticationPrincipal Jwt jwt) {
        return identities.execute(me(jwt)).stream().map(ApiMapper::identity).toList();
    }

    @DeleteMapping("/identities/{identityUuid}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlink(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID identityUuid) { unlinkIdentity.execute(me(jwt), identityUuid); }
}
