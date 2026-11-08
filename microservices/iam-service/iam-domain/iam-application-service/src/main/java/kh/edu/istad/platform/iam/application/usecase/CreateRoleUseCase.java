package kh.edu.istad.platform.iam.application.usecase;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Transactional
public class CreateRoleUseCase {
    private final RoleRepository roles;
    private final RoleAssembler assembler;
    private final RolePermissionAssigner assigner;
    private final IamDomainService iamDomainService;

    public RoleResult execute(CreateRoleCommand c) {
        String name = c.name().trim().toUpperCase(Locale.ROOT);
        if (roles.existsByName(name)) throw new ConflictException("role-name-taken", "Role already exists: " + name);
        Role r = new Role();
        r.setName(name); r.setDescription(c.description());

        // Domain service validates business rules and returns the creation event
        iamDomainService.createRole(r);

        r = roles.save(r);
        if (c.permissionUuids() != null && !c.permissionUuids().isEmpty())
            assigner.assign(r, c.permissionUuids(), null, "Initial assignment");
        return assembler.result(r);
    }
}
