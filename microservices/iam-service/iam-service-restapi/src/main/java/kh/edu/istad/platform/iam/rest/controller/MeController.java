package kh.edu.istad.platform.iam.rest.controller;

import jakarta.validation.Valid;
import java.util.*;
import kh.edu.istad.platform.iam.application.command.*;
import kh.edu.istad.platform.iam.application.usecase.*;
import kh.edu.istad.platform.iam.rest.dto.*;
import kh.edu.istad.platform.iam.rest.dto.Common.PageResponse;
import kh.edu.istad.platform.iam.rest.dto.MonitoringDtos.*;
import kh.edu.istad.platform.iam.rest.dto.UserDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {
    private final GetUserUseCase get;
    private final UpdateUserProfileUseCase update;
    private final ChangePasswordUseCase changePassword;
    private final ListUserIdentitiesUseCase identities;
    private final UnlinkIdentityUseCase unlinkIdentity;
    private final GetUserSessionsUseCase getSessions;
    private final TerminateSessionUseCase terminateSession;
    private final TerminateAllSessionsUseCase terminateAllSessions;
    private final GetLoginHistoryUseCase getLoginHistory;
    private final GetActivityUseCase getActivity;
    private final GetDeactivationRequestUseCase getDeactivation;
    private final RequestDeactivationUseCase requestDeactivation;
    private final CancelDeactivationUseCase cancelDeactivation;

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

    // --- Sessions (/api/me/sessions) ---
    @GetMapping("/sessions")
    public List<SessionResponse> sessions(@AuthenticationPrincipal Jwt jwt) {
        String username = get.execute(me(jwt)).username();
        return getSessions.execute(username, jwt.getId()).stream()
            .map(s -> new SessionResponse(s.id(), s.device(), s.current(), s.browser(), s.os(), s.ipAddress(), s.location(), s.lastActiveAt(), s.createdAt()))
            .toList();
    }

    @DeleteMapping("/sessions/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void terminateSession(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
        String username = get.execute(me(jwt)).username();
        terminateSession.execute(username, id);
    }

    @DeleteMapping("/sessions") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void terminateAllSessions(@AuthenticationPrincipal Jwt jwt) {
        String username = get.execute(me(jwt)).username();
        terminateAllSessions.execute(username);
    }

    // --- Login History (/api/me/login-history) ---
    @GetMapping("/login-history")
    public PageResponse<LoginRecordResponse> loginHistory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "occurredAt,desc") String sort) {
        String username = get.execute(me(jwt)).username();
        var result = getLoginHistory.executeForUser(username, status, page, size, sort);
        var mapped = result.map(r -> new LoginRecordResponse(
            r.getId(), r.getUsername(), r.getStatus(), r.getIpAddress(), r.getLocation(),
            r.getUserAgent(), r.getDevice(), r.getBrowser(), r.getOs(), r.isSuspicious(), r.getFailureReason(), r.getOccurredAt()));
        return PageResponse.of(mapped);
    }

    // --- Activity (/api/me/activity) ---
    @GetMapping("/activity")
    public PageResponse<ActivityResponse> activity(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "occurredAt,desc") String sort) {
        var result = getActivity.execute(me(jwt), page, size, sort);
        var mapped = result.map(a -> new ActivityResponse(a.getId(), a.getAction(), a.getDetail(), a.getIpAddress(), a.getOccurredAt()));
        return PageResponse.of(mapped);
    }

    // --- Deactivation Request (/api/me/deactivation-request) ---
    @GetMapping("/deactivation-request")
    public ResponseEntity<DeactivationResponse> deactivation(@AuthenticationPrincipal Jwt jwt) {
        return getDeactivation.execute(me(jwt))
            .map(r -> ResponseEntity.ok(new DeactivationResponse(r.getStatus(), r.getRequestedAt())))
            .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/deactivation-request")
    public DeactivationResponse requestDeactivation(@AuthenticationPrincipal Jwt jwt, @RequestBody DeactivationRequestDto dto) {
        var r = requestDeactivation.execute(me(jwt), dto.reason(), dto.details());
        return new DeactivationResponse(r.getStatus(), r.getRequestedAt());
    }

    @DeleteMapping("/deactivation-request") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelDeactivation(@AuthenticationPrincipal Jwt jwt) {
        cancelDeactivation.execute(me(jwt));
    }
}
