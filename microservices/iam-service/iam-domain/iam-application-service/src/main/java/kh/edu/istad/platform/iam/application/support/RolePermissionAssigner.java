package kh.edu.istad.platform.iam.application.support;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
@Component @RequiredArgsConstructor
public class RolePermissionAssigner {
    private final RoleRepository roles;
    private final PermissionRepository permissions;
    private final CurrentActorProvider actor;

    public List<RolePermission> assign(Role role, Collection<UUID> permissionUuids, Instant expiresAt, String reason) {
        role.assertMutable();
        Set<UUID> wanted = new LinkedHashSet<>(permissionUuids);
        List<Permission> found = permissions.findByUuids(wanted);
        if (found.size() != wanted.size()) throw new NotFoundException("permission-not-found", "One or more permissions were not found");
        Instant now = Instant.now();
        Set<Long> already = roles.findRolePermissions(role.getId()).stream()
            .filter(a -> a.isActive(now) && a.getPermission() != null).map(a -> a.getPermission().getId()).collect(Collectors.toSet());
        List<RolePermission> created = new ArrayList<>();
        for (Permission p : found) {
            p.assertAssignable();
            if (already.contains(p.getId())) continue;
            created.add(roles.saveRolePermission(RolePermission.create(role.getId(), p, actor.currentActor(), expiresAt, reason)));
        }
        return created;
    }
}
