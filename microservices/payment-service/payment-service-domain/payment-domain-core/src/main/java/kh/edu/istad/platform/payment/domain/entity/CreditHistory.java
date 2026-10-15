package kh.edu.istad.platform.payment.domain.entity;

import kh.edu.istad.common.entity.BaseEntity;
import kh.edu.istad.common.valueobject.CustomerId;
import kh.edu.istad.common.valueobject.Money;
import kh.edu.istad.platform.payment.domain.valueobject.CreditHistoryId;
import kh.edu.istad.platform.payment.domain.valueobject.TransactionType;

public class CreditHistory extends BaseEntity<CreditHistoryId> {

    private final CustomerId customerId;
    private final Money amount;
    private final TransactionType transactionType;

    public CreditHistory(CreditHistoryId creditHistoryId,CustomerId customerId, Money amount, TransactionType transactionType) {
        super.setId(creditHistoryId);
        this.customerId = customerId;
        this.amount = amount;
        this.transactionType = transactionType;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public Money getAmount() {
        return amount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }
}
