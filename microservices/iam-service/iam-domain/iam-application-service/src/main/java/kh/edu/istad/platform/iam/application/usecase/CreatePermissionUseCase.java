package kh.edu.istad.platform.iam.application.usecase;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Transactional
public class CreatePermissionUseCase {
    private final PermissionRepository permissions;
    private final IamDomainService iamDomainService;

    public PermissionResult execute(CreatePermissionCommand c) {
        String name = Permission.nameOf(c.resource(), c.action());
        if (permissions.existsByName(name)) throw new ConflictException("permission-name-taken", "Permission already exists: " + name);
        Permission p = new Permission();
        p.setResource(c.resource().trim().toLowerCase(Locale.ROOT));
        p.setAction(c.action().trim().toLowerCase(Locale.ROOT));
        p.setDescription(c.description());

        // Domain service validates business rules (rebuilds composite name) and returns the creation event
        iamDomainService.createPermission(p);

        return RoleMapper.toPermission(permissions.save(p));
    }
}
