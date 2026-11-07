package kh.edu.istad.platform.iam.application.usecase;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Transactional
public class UpdatePermissionUseCase {
    private final PermissionRepository permissions;

    public PermissionResult execute(UpdatePermissionCommand c) {
        Permission p = permissions.findByUuid(c.permissionUuid()).orElseThrow(() -> NotFoundException.of("Permission", c.permissionUuid()));
        p.assertMutable();
        if (c.description() != null) p.setDescription(c.description());
        return RoleMapper.toPermission(permissions.save(p));
    }
}
