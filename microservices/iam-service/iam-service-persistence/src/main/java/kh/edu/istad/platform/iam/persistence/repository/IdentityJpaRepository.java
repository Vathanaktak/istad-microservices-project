package kh.edu.istad.platform.iam.persistence.repository;
import kh.edu.istad.platform.iam.persistence.entity.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface IdentityJpaRepository extends JpaRepository<IdentityEntity, Long> {
    Optional<IdentityEntity> findByProviderAndProviderSubject(kh.edu.istad.platform.iam.domain.model.IdentityProvider provider, String subject);
    List<IdentityEntity> findByUserIdOrderByLinkedAtAsc(Long userId);
}
