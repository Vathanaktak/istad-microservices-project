package kh.edu.istad.platform.iam.persistence.repository;

import java.util.Optional;
import java.util.UUID;
import kh.edu.istad.platform.iam.persistence.entity.DeactivationRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeactivationRequestJpaRepository extends JpaRepository<DeactivationRequestEntity, Long> {
    Optional<DeactivationRequestEntity> findFirstByUserIdAndStatusOrderByRequestedAtDesc(Long userId, String status);
    Optional<DeactivationRequestEntity> findByUuid(UUID uuid);
    Page<DeactivationRequestEntity> findByStatusOrderByRequestedAtDesc(String status, Pageable pageable);
    Page<DeactivationRequestEntity> findAllByOrderByRequestedAtDesc(Pageable pageable);
    long countByStatus(String status);
    boolean existsByUserIdAndStatus(Long userId, String status);
}
