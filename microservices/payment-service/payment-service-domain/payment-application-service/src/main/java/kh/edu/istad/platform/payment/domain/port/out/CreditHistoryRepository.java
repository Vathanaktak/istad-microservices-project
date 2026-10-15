package kh.edu.istad.platform.payment.domain.port.out;

import kh.edu.istad.common.valueobject.CustomerId;
import kh.edu.istad.platform.payment.domain.entity.CreditHistory;

import java.util.List;
import java.util.Optional;

public interface CreditHistoryRepository {

    CreditHistory save(CreditHistory creditHistory);
    List<CreditHistory> findByCustomerId(CustomerId customerId);
}
