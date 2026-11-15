package kh.edu.istad.platform.iam.persistence.repository;

import kh.edu.istad.platform.iam.persistence.entity.LoginHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LoginHistoryJpaRepository extends JpaRepository<LoginHistoryEntity, Long>, JpaSpecificationExecutor<LoginHistoryEntity> {
    Page<LoginHistoryEntity> findByUsernameIgnoreCase(String username, Pageable pageable);
    Page<LoginHistoryEntity> findByUsernameIgnoreCaseAndStatus(String username, String status, Pageable pageable);
}
