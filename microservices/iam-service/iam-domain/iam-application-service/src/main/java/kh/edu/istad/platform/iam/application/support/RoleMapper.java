package kh.edu.istad.platform.iam.application.support;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
public final class RoleMapper {
    private RoleMapper() {}

    public static PermissionResult toPermission(Permission p) {
        return new PermissionResult(p.getUuid(), p.getName(), p.getResource(), p.getAction(), p.getDescription(), p.isSystem(), p.isActive());
    }
    public static RoleSummary toSummary(Role r) {
        return new RoleSummary(r.getUuid(), r.getName(), r.getDescription(), r.isSystem(), r.isActive());
    }
    public static RoleResult toResult(Role r, List<RolePermission> activeAssignments) {
        return new RoleResult(r.getUuid(), r.getName(), r.getDescription(), r.isSystem(), r.isActive(),
            activeAssignments.stream().map(a -> toPermission(a.getPermission())).toList(), r.getCreatedAt(), r.getUpdatedAt());
    }
    public static PermissionAssignmentResult toAssignment(RolePermission a) {
        return new PermissionAssignmentResult(a.getPermission().getUuid(), a.getPermission().getName(), a.getAssignedAt(), a.getExpiresAt(), a.getReason());
    }
}
