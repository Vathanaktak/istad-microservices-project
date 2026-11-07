package kh.edu.istad.platform.iam.application.usecase;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Transactional
public class GrantPermissionsToUserUseCase {
    private final UserAssembler assembler;
    private final UserRepository users;
    private final PermissionRepository permissions;
    private final CurrentActorProvider actor;

    public List<PermissionGrantResult> execute(GrantPermissionsToUserCommand c) {
        User u = assembler.require(c.userUuid());
        Effect effect = c.effect() == null ? Effect.GRANT : c.effect();
        Set<UUID> wanted = new LinkedHashSet<>(c.permissionUuids());
        List<Permission> found = permissions.findByUuids(wanted);
        if (found.size() != wanted.size()) throw new NotFoundException("permission-not-found", "One or more permissions were not found");
        Instant now = Instant.now();
        List<UserPermission> existing = users.findUserPermissions(u.getId());
        List<PermissionGrantResult> out = new ArrayList<>();
        for (Permission p : found) {
            p.assertAssignable();
            Optional<UserPermission> current = existing.stream()
                .filter(x -> x.getPermission() != null && x.getPermission().getId().equals(p.getId()) && x.isActive(now)).findFirst();
            if (current.isPresent()) {
                if (current.get().getEffect() == effect) continue;
                current.get().revoke(actor.currentActor(), "Replaced by " + effect);
                users.saveUserPermission(current.get());
            }
            out.add(UserMapper.toGrant(users.saveUserPermission(
                UserPermission.create(u.getId(), p, effect, actor.currentActor(), c.expiresAt(), c.reason()))));
        }
        return out;
    }
}
