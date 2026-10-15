package kh.edu.istad.platform.payment.persistence.adapter;

import kh.edu.istad.common.valueobject.CustomerId;
import kh.edu.istad.common.valueobject.Money;
import kh.edu.istad.platform.payment.domain.entity.CreditHistory;
import kh.edu.istad.platform.payment.domain.port.out.CreditHistoryRepository;
import kh.edu.istad.platform.payment.domain.valueobject.CreditHistoryId;
import kh.edu.istad.platform.payment.persistence.entity.CreditHistoryJpaEntity;
import kh.edu.istad.platform.payment.persistence.repository.CreditHistoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CreditHistoryRepositoryAdapter implements CreditHistoryRepository {

    private final CreditHistoryJpaRepository creditHistoryJpaRepository;

    @Override
    public CreditHistory save(CreditHistory creditHistory) {

        CreditHistoryJpaEntity entity = CreditHistoryJpaEntity.builder()
                .id(creditHistory.getId().value())
                .customerId(creditHistory.getCustomerId().value())
                .amount(creditHistory.getAmount().getAmount())
                .type(creditHistory.getTransactionType())
                .build();
        CreditHistoryJpaEntity saved = creditHistoryJpaRepository.save(entity);

        return toDomain(saved);
    }

    @Override
    public List<CreditHistory> findByCustomerId(CustomerId customerId) {

        return creditHistoryJpaRepository.findByCustomerId(customerId.value())
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private CreditHistory toDomain(CreditHistoryJpaEntity entity) {
        return new CreditHistory(
                new CreditHistoryId(entity.getId()),
                new CustomerId(entity.getCustomerId()),
                new Money(entity.getAmount()),
                entity.getType()
        );
    }
}
