package kh.edu.istad.platform.iam.application.support;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
@Component @RequiredArgsConstructor
public class UserRoleAssigner {
    private final UserRepository users;
    private final RoleRepository roles;
    private final CurrentActorProvider actor;

    public List<UserRole> assign(User user, Collection<UUID> roleUuids, Instant expiresAt, String reason) {
        Set<UUID> wanted = new LinkedHashSet<>(roleUuids);
        List<Role> found = roles.findByUuids(wanted);
        if (found.size() != wanted.size()) throw new NotFoundException("role-not-found", "One or more roles were not found");
        Instant now = Instant.now();
        Set<Long> already = users.findUserRoles(user.getId()).stream()
            .filter(a -> a.isActive(now) && a.getRole() != null).map(a -> a.getRole().getId()).collect(Collectors.toSet());
        List<UserRole> created = new ArrayList<>();
        for (Role r : found) {
            r.assertAssignable();
            if (already.contains(r.getId())) continue;
            created.add(users.saveUserRole(UserRole.create(user.getId(), r, actor.currentActor(), expiresAt, reason)));
        }
        return created;
    }
}
