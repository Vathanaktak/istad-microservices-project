package kh.edu.istad.platform.iam.application.usecase;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Transactional
public class DeletePermissionUseCase {
    private final PermissionRepository permissions;
    private final CurrentActorProvider actor;

    public void execute(UUID permissionUuid) {
        Permission p = permissions.findByUuid(permissionUuid).orElseThrow(() -> NotFoundException.of("Permission", permissionUuid));
        p.assertMutable();
        p.softDelete(actor.currentActor());
        p.setActive(false);
        permissions.save(p);
    }
}
