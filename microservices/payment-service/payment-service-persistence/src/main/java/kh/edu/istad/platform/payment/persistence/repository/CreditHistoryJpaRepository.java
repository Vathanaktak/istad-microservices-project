package kh.edu.istad.platform.payment.persistence.repository;

import kh.edu.istad.platform.payment.persistence.entity.CreditHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CreditHistoryJpaRepository
extends JpaRepository<CreditHistoryJpaEntity, UUID> {

    List<CreditHistoryJpaEntity> findByCustomerId(UUID customerId);

}
