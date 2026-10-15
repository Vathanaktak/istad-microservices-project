package kh.edu.istad.platform.payment.domain.entity;

import kh.edu.istad.common.entity.AggregateRoot;
import kh.edu.istad.common.valueobject.CustomerId;
import kh.edu.istad.common.valueobject.Money;
import kh.edu.istad.common.valueobject.OrderId;
import kh.edu.istad.platform.payment.domain.exception.PaymentDomainException;
import kh.edu.istad.platform.payment.domain.valueobject.PaymentId;
import kh.edu.istad.platform.payment.domain.valueobject.PaymentStatus;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class Payment extends AggregateRoot<PaymentId> {

    private final CustomerId customerId;
    private final OrderId orderId;
    private Money price;
    private PaymentStatus paymentStatus;
    private ZonedDateTime createdAt;

    public Payment(PaymentId paymentId,CustomerId customerId, OrderId orderId, Money price, PaymentStatus paymentStatus, ZonedDateTime createdAt) {
        super.setId(paymentId);
        this.customerId = customerId;
        this.orderId = orderId;
        this.price = price;
        this.paymentStatus = paymentStatus;
        this.createdAt = createdAt;
    }

    public void initializePayment(){
        setId(new PaymentId(UUID.randomUUID()));
        this.createdAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }

//    public void validatePayment(){
//        if(price == null ){
//            throw new PaymentDomainException("Money must be not null and granter than 0");
//        }
//        if(customerId.value() == null){
//            throw new PaymentDomainException("customer Id must be not null");
//        }
//        if(orderId.value() == null){
//            throw new PaymentDomainException("order Id must be not null");
//        }
//    }

    public void validatePayment(List<String> failureMessages) {
        if (price == null) {
            failureMessages.add("Total price must be greater than zero!");
        }
    }
    public void updateStatus(PaymentStatus status){
        this.paymentStatus = status;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public Money getPrice() {
        return price;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }
}
