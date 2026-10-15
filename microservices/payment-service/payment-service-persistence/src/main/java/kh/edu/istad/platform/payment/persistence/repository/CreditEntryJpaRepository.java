package kh.edu.istad.platform.payment.persistence.repository;

import kh.edu.istad.platform.payment.domain.entity.CreditEntry;
import kh.edu.istad.platform.payment.persistence.entity.CreditEntryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditEntryJpaRepository
    extends JpaRepository<CreditEntryJpaEntity, UUID> {

    Optional<CreditEntryJpaEntity> findByCustomerId(UUID customerId);
}
