package kh.edu.istad.platform.iam.persistence.repository;
import kh.edu.istad.platform.iam.persistence.entity.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface UserRoleJpaRepository extends JpaRepository<UserRoleEntity, Long> {
    @Query("select ur from UserRoleEntity ur join fetch ur.role r where ur.userId = :userId")
    List<UserRoleEntity> findAllWithRole(@Param("userId") Long userId);
}
