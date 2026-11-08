package kh.edu.istad.platform.iam.application.usecase;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Transactional
public class UpdateRoleUseCase {
    private final RoleAssembler assembler;
    private final RoleRepository roles;
    private final IamDomainService iamDomainService;

    public RoleResult execute(UpdateRoleCommand c) {
        Role r = assembler.require(c.roleUuid());
        if (c.name() != null) {
            String name = c.name().trim().toUpperCase(Locale.ROOT);
            if (!name.equals(r.getName())) {
                if (roles.existsByName(name)) throw new ConflictException("role-name-taken", "Role already exists: " + name);
            }
        }

        // Domain service validates (assertMutable) and applies update
        iamDomainService.updateRole(r, c.name(), c.description());

        return assembler.result(roles.save(r));
    }
}
