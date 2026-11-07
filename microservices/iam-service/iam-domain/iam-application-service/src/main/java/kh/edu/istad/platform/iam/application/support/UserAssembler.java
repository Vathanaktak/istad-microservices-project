package kh.edu.istad.platform.iam.application.support;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
@Component @RequiredArgsConstructor
public class UserAssembler {
    private final UserRepository users;
    private final RoleRepository roles;

    public record Resolved(List<UserRole> roles, List<UserPermission> directPermissions, AuthorityCalculator.Result authorities) {}

    public User require(UUID uuid) {
        return users.findByUuid(uuid).orElseThrow(() -> NotFoundException.of("User", uuid));
    }
    public Resolved resolve(User u) {
        Instant now = Instant.now();
        List<UserRole> active = users.findUserRoles(u.getId()).stream()
            .filter(a -> a.isActive(now) && a.getRole() != null && a.getRole().isActive()).toList();
        List<Long> roleIds = active.stream().map(a -> a.getRole().getId()).toList();
        List<RolePermission> rp = roleIds.isEmpty() ? List.of() : roles.findRolePermissionsByRoleIds(roleIds);
        List<UserPermission> up = users.findUserPermissions(u.getId()).stream().filter(a -> a.isActive(now)).toList();
        return new Resolved(active, up, AuthorityCalculator.calculate(active, rp, up, now));
    }
    public UserResult result(User u) { return UserMapper.toResult(u, resolve(u).roles()); }
    public AuthUserResult authUser(User u) {
        Resolved r = resolve(u);
        return new AuthUserResult(UserMapper.toResult(u, r.roles()), u.getPasswordHash(), u.isAccountNonExpired(),
            u.isCredentialsNonExpired(), r.authorities().roles(), r.authorities().permissions());
    }
}
