package kh.edu.istad.platform.iam.application.usecase;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Transactional
public class RevokePermissionFromUserUseCase {
    private final UserAssembler assembler;
    private final UserRepository users;
    private final CurrentActorProvider actor;

    public void execute(UUID userUuid, UUID permissionUuid, String reason) {
        User u = assembler.require(userUuid);
        Instant now = Instant.now();
        UserPermission a = users.findUserPermissions(u.getId()).stream()
            .filter(x -> x.getPermission() != null && x.getPermission().getUuid().equals(permissionUuid) && x.isActive(now)).findFirst()
            .orElseThrow(() -> NotFoundException.of("PermissionAssignment", permissionUuid));
        a.revoke(actor.currentActor(), reason);
        users.saveUserPermission(a);
    }
}
