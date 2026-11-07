package kh.edu.istad.platform.iam.persistence.repository;
import kh.edu.istad.platform.iam.persistence.entity.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface RoleJpaRepository extends JpaRepository<RoleEntity, Long>, JpaSpecificationExecutor<RoleEntity> {
    Optional<RoleEntity> findByUuid(UUID uuid);
    List<RoleEntity> findByUuidIn(Collection<UUID> uuids);
    Optional<RoleEntity> findByName(String name);
    boolean existsByName(String name);
}
