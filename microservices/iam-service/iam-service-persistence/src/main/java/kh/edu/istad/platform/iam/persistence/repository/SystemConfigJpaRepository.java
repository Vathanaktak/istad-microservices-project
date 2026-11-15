package kh.edu.istad.platform.iam.persistence.repository;

import java.util.Optional;
import kh.edu.istad.platform.iam.persistence.entity.SystemConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemConfigJpaRepository extends JpaRepository<SystemConfigEntity, Long> {
    Optional<SystemConfigEntity> findFirstByOrderByIdDesc();
}
