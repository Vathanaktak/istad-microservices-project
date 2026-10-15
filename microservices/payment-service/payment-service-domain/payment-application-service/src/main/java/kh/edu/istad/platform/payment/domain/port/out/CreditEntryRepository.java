package kh.edu.istad.platform.payment.domain.port.out;

import kh.edu.istad.common.valueobject.CustomerId;
import kh.edu.istad.platform.payment.domain.entity.CreditEntry;

import java.util.Optional;
import java.util.UUID;

public interface CreditEntryRepository {

    CreditEntry save(CreditEntry creditEntry);
    Optional<CreditEntry> findByCustomerId(CustomerId customerId);
}
