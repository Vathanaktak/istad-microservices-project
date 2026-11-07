package kh.edu.istad.platform.iam.application.support;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
@Component @RequiredArgsConstructor
public class RoleAssembler {
    private final RoleRepository roles;

    public Role require(UUID uuid) { return roles.findByUuid(uuid).orElseThrow(() -> NotFoundException.of("Role", uuid)); }
    public RoleResult result(Role r) {
        Instant now = Instant.now();
        return RoleMapper.toResult(r, roles.findRolePermissions(r.getId()).stream()
            .filter(a -> a.isActive(now) && a.getPermission() != null).toList());
    }
}
