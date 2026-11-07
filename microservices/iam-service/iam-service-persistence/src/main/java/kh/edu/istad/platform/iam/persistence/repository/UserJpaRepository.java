package kh.edu.istad.platform.iam.persistence.repository;
import kh.edu.istad.platform.iam.persistence.entity.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface UserJpaRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {
    Optional<UserEntity> findByUuid(UUID uuid);
    @Query("select u from UserEntity u where lower(u.username) = lower(:v) or u.email = lower(:v)")
    Optional<UserEntity> findByUsernameOrEmail(@Param("v") String v);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
}
