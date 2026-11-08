package kh.edu.istad.platform.iam.application.usecase;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Transactional
public class DeleteRoleUseCase {
    private final RoleAssembler assembler;
    private final RoleRepository roles;
    private final CurrentActorProvider actor;
    private final IamDomainService iamDomainService;

    public void execute(UUID roleUuid) {
        Role r = assembler.require(roleUuid);

        // Domain service validates (assertMutable + not already deleted) and soft-deletes
        iamDomainService.deleteRole(r, actor.currentActor());

        roles.save(r);
    }
}
