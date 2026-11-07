package kh.edu.istad.platform.iam.application.usecase;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Transactional
public class AssignRolesToUserUseCase {
    private final UserAssembler assembler;
    private final UserRoleAssigner assigner;

    public List<RoleAssignmentResult> execute(AssignRolesToUserCommand c) {
        User u = assembler.require(c.userUuid());
        return assigner.assign(u, c.roleUuids(), c.expiresAt(), c.reason()).stream().map(UserMapper::toRoleAssignment).toList();
    }
}
