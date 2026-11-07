package kh.edu.istad.platform.iam.application.support;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
public final class UserMapper {
    private UserMapper() {}

    public static UserResult toResult(User u, List<UserRole> activeRoles) {
        return new UserResult(u.getUuid(), u.getUsername(), u.getEmail(), u.isEmailVerified(), u.fullName(), u.getGivenName(),
            u.getFamilyName(), u.getMiddleName(), u.getNickname(), u.getGender(), u.getBirthdate(), u.getProfileUrl(),
            u.getPictureUrl(), u.getWebsiteUrl(), u.getZoneinfo(), u.getLocale(), u.getPhoneNumber(), u.isPhoneNumberVerified(),
            new AddressResult(u.getAddressStreetAddress(), u.getAddressLocality(), u.getAddressRegion(), u.getAddressPostalCode(), u.getAddressCountry()),
            u.isEnabled(), u.isAccountNonLocked(),
            activeRoles.stream().map(r -> new RoleRef(r.getRole().getUuid(), r.getRole().getName())).toList(),
            u.getCreatedAt(), u.getUpdatedAt());
    }
    public static UserSummary toSummary(User u) {
        return new UserSummary(u.getUuid(), u.getUsername(), u.getEmail(), u.fullName(), u.isEnabled(), u.isAccountNonLocked());
    }
    public static RoleAssignmentResult toRoleAssignment(UserRole a) {
        return new RoleAssignmentResult(a.getRole().getUuid(), a.getRole().getName(), a.getAssignedAt(), a.getExpiresAt(), a.getReason());
    }
    public static PermissionGrantResult toGrant(UserPermission a) {
        return new PermissionGrantResult(a.getPermission().getUuid(), a.getPermission().getName(), a.getEffect().name(),
            a.getAssignedAt(), a.getExpiresAt(), a.getReason());
    }
    public static IdentityResult toIdentity(Identity i) {
        return new IdentityResult(i.getUuid(), i.getProvider().name(), i.getEmail(), i.getDisplayName(), i.getUsername(),
            i.getPictureUrl(), i.getLinkedAt(), i.getLastLoginAt());
    }
}
