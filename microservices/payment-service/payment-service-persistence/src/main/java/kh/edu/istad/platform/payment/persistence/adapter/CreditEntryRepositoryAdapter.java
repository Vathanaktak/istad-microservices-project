package kh.edu.istad.platform.payment.persistence.adapter;

import kh.edu.istad.common.valueobject.CustomerId;
import kh.edu.istad.common.valueobject.Money;
import kh.edu.istad.platform.payment.domain.entity.CreditEntry;
import kh.edu.istad.platform.payment.domain.port.out.CreditEntryRepository;
import kh.edu.istad.platform.payment.domain.valueobject.CreditEntryId;
import kh.edu.istad.platform.payment.persistence.entity.CreditEntryJpaEntity;
import kh.edu.istad.platform.payment.persistence.repository.CreditEntryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CreditEntryRepositoryAdapter implements CreditEntryRepository {

    private final CreditEntryJpaRepository creditEntryJpaRepository;
    @Override
    public CreditEntry save(CreditEntry creditEntry) {

        CreditEntryJpaEntity entity = CreditEntryJpaEntity.builder()
                .id(creditEntry.getId().value())
                .customerId(creditEntry.getCustomerId().value())
                .totalCreditAmount(creditEntry.getTotalCreditAmount().getAmount())
                .build();
        CreditEntryJpaEntity saved = creditEntryJpaRepository.save(entity);

        return toDomain(saved);
    }

    @Override
    public Optional<CreditEntry> findByCustomerId(CustomerId customerId) {

        return creditEntryJpaRepository.findByCustomerId(customerId.value())
                .map(this::toDomain);
    }

    private CreditEntry toDomain(CreditEntryJpaEntity entity) {
        return new CreditEntry(
                new CreditEntryId(entity.getId()),
                new CustomerId(entity.getCustomerId()),
                new Money(entity.getTotalCreditAmount())
        );
    }
}
