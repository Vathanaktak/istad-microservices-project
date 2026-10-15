package kh.edu.istad.platform.payment.domain.event;

import kh.edu.istad.platform.payment.domain.entity.Payment;

import java.time.ZonedDateTime;
import java.util.List;

public class PaymentFailedEvent extends PaymentEvent{
    public PaymentFailedEvent(Payment payment, ZonedDateTime createdAt, List<String> failureMessage) {
        super(payment, createdAt, failureMessage);
    }
}
