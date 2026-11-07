package kh.edu.istad.platform.iam.persistence.repository;
import kh.edu.istad.platform.iam.persistence.entity.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface UserPermissionJpaRepository extends JpaRepository<UserPermissionEntity, Long> {
    @Query("select up from UserPermissionEntity up join fetch up.permission p where up.userId = :userId")
    List<UserPermissionEntity> findAllWithPermission(@Param("userId") Long userId);
}
