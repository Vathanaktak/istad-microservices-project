package kh.edu.istad.platform.iam.persistence.repository;
import kh.edu.istad.platform.iam.persistence.entity.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface RolePermissionJpaRepository extends JpaRepository<RolePermissionEntity, Long> {
    @Query("select rp from RolePermissionEntity rp join fetch rp.permission p where rp.roleId in :roleIds")
    List<RolePermissionEntity> findAllWithPermission(@Param("roleIds") Collection<Long> roleIds);
}
