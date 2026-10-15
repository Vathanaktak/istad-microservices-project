package kh.edu.istad.platform.payment.domain.entity;

import kh.edu.istad.common.entity.BaseEntity;
import kh.edu.istad.common.valueobject.CustomerId;
import kh.edu.istad.common.valueobject.Money;
import kh.edu.istad.platform.payment.domain.valueobject.CreditEntryId;

public class CreditEntry extends BaseEntity<CreditEntryId> {

    private final CustomerId customerId;
    private Money totalCreditAmount;

    public CreditEntry(CreditEntryId creditEntryId,CustomerId customerId, Money totalCreditAmount) {
        super.setId(creditEntryId);
        this.customerId = customerId;
        this.totalCreditAmount = totalCreditAmount;
    }

    public void addCreditAmount(Money amount){
        this.totalCreditAmount = this.totalCreditAmount.add(amount);
    }

    public void subtractCreditAmount(Money amount){
        this.totalCreditAmount = this.totalCreditAmount.subtract(amount);
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public Money getTotalCreditAmount() {
        return totalCreditAmount;
    }
}
