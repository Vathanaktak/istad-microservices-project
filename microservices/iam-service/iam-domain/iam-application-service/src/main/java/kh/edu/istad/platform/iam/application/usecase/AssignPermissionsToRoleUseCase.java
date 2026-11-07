package kh.edu.istad.platform.iam.application.usecase;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Transactional
public class AssignPermissionsToRoleUseCase {
    private final RoleAssembler assembler;
    private final RolePermissionAssigner assigner;

    public List<PermissionAssignmentResult> execute(AssignPermissionsToRoleCommand c) {
        Role r = assembler.require(c.roleUuid());
        return assigner.assign(r, c.permissionUuids(), c.expiresAt(), c.reason()).stream().map(RoleMapper::toAssignment).toList();
    }
}
