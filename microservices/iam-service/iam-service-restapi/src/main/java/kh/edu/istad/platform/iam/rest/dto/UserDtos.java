package kh.edu.istad.platform.iam.rest.dto;

import static kh.edu.istad.platform.iam.rest.dto.Common.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kh.edu.istad.platform.iam.domain.model.Effect;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class UserDtos {
    private UserDtos() {}

    public record AddressDto(@Size(max = 255) String streetAddress, @Size(max = 100) String locality, @Size(max = 100) String region,
                             @Size(max = 20) String postalCode, @Pattern(regexp = "^[A-Z]{2}$", message = "must be ISO 3166-1 alpha-2") String country) {}

    public record RegisterUserRequest(
        @NotBlank @Size(min = 3, max = 50) @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "letters, digits, . _ - only") String username,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Pattern(regexp = PASSWORD_REGEX, message = PASSWORD_MSG) String password,
        @NotBlank String confirmPassword,
        @NotBlank @Size(max = 100) String givenName,
        @NotBlank @Size(max = 100) String familyName,
        @Pattern(regexp = "^(en|km)$", message = "must be en or km") String locale) {
        @JsonIgnore @AssertTrue(message = "passwords do not match")
        public boolean isConfirmPasswordValid() { return password != null && password.equals(confirmPassword); }
    }

    public record CreateUserRequest(
        @NotBlank @Size(min = 3, max = 50) @Pattern(regexp = "^[A-Za-z0-9._-]+$") String username,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Pattern(regexp = PASSWORD_REGEX, message = PASSWORD_MSG) String password,
        @NotBlank @Size(max = 100) String givenName,
        @NotBlank @Size(max = 100) String familyName,
        @Pattern(regexp = "^(female|male|other)$") String gender,
        @Past LocalDate birthdate,
        @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "must be E.164, e.g. +85512345678") String phoneNumber,
        @Size(max = 50) String zoneinfo,
        @Pattern(regexp = "^(en|km)$", message = "must be en or km") String locale,
        @Valid AddressDto address,
        List<UUID> roleUuids) {}

    public record UpdateUserProfileRequest(
        @Size(max = 100) String givenName, @Size(max = 100) String familyName, @Size(max = 100) String middleName,
        @Size(max = 100) String nickname, @Pattern(regexp = "^(female|male|other)$") String gender, @Past LocalDate birthdate,
        @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "must be E.164, e.g. +85512345678") String phoneNumber,
        @Size(max = 500) String profileUrl, @Size(max = 500) String pictureUrl, @Size(max = 500) String websiteUrl,
        @Size(max = 50) String zoneinfo, @Pattern(regexp = "^(en|km)$", message = "must be en or km") String locale,
        @Valid AddressDto address) {}

    public record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank @Pattern(regexp = PASSWORD_REGEX, message = PASSWORD_MSG) String newPassword,
        @NotBlank String confirmPassword) {
        @JsonIgnore @AssertTrue(message = "passwords do not match")
        public boolean isConfirmPasswordValid() { return newPassword != null && newPassword.equals(confirmPassword); }
    }

    public record ResetPasswordRequest(
        @NotBlank @Pattern(regexp = PASSWORD_REGEX, message = PASSWORD_MSG) String newPassword,
        @NotBlank String confirmPassword) {
        @JsonIgnore @AssertTrue(message = "passwords do not match")
        public boolean isConfirmPasswordValid() { return newPassword != null && newPassword.equals(confirmPassword); }
    }

    public record AssignRolesToUserRequest(@NotEmpty List<@NotNull UUID> roleUuids, @Future Instant expiresAt, @Size(max = 255) String reason) {}
    public record GrantPermissionsToUserRequest(@NotEmpty List<@NotNull UUID> permissionUuids, Effect effect,
                                                @Future Instant expiresAt, @Size(max = 255) String reason) {}

    public record RoleRefResponse(UUID uuid, String name) {}
    public record UserResponse(UUID uuid, String username, String email, boolean emailVerified, String name, String givenName,
                               String familyName, String locale, boolean enabled, List<RoleRefResponse> roles, Instant createdAt, Instant updatedAt) {}
    public record UserSummaryResponse(UUID uuid, String username, String email, String name, boolean enabled, boolean locked) {}

    public record RoleAssignmentResponse(UUID uuid, String name, Instant assignedAt, Instant expiresAt, String reason) {}
    public record PermissionGrantResponse(UUID uuid, String name, String effect, Instant expiresAt) {}
    public record UserAuthoritiesResponse(List<RoleAssignmentResponse> roles, List<PermissionGrantResponse> directPermissions,
                                          java.util.Set<String> effectivePermissions) {}
    public record IdentityResponse(UUID uuid, String provider, String email, String displayName, Instant linkedAt) {}
}
