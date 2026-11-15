package kh.edu.istad.platform.iam.persistence.repository;

import java.util.Optional;
import kh.edu.istad.platform.iam.persistence.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenEntity, Long> {
    Optional<PasswordResetTokenEntity> findByTokenHashAndUsedFalse(String tokenHash);

    @Modifying
    @Query("UPDATE PasswordResetTokenEntity t SET t.used = true, t.usedAt = CURRENT_TIMESTAMP WHERE t.userId = :userId AND t.used = false")
    void invalidateAllByUserId(Long userId);
}
