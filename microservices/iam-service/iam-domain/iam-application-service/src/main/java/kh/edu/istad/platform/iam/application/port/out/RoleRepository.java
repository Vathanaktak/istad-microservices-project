package kh.edu.istad.platform.iam.application.port.out;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*;
import java.time.*; import java.util.*;
public interface RoleRepository {
    Optional<Role> findByUuid(UUID uuid);
    List<Role> findByUuids(Collection<UUID> uuids);
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
    Role save(Role role);
    PageResult<Role> search(RoleFilterQuery query);

    List<RolePermission> findRolePermissions(Long roleId);
    List<RolePermission> findRolePermissionsByRoleIds(Collection<Long> roleIds);
    RolePermission saveRolePermission(RolePermission assignment);
}
