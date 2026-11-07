package kh.edu.istad.platform.iam.persistence.repository;
import kh.edu.istad.platform.iam.persistence.entity.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface PermissionJpaRepository extends JpaRepository<PermissionEntity, Long>, JpaSpecificationExecutor<PermissionEntity> {
    Optional<PermissionEntity> findByUuid(UUID uuid);
    List<PermissionEntity> findByUuidIn(Collection<UUID> uuids);
    boolean existsByName(String name);
}
