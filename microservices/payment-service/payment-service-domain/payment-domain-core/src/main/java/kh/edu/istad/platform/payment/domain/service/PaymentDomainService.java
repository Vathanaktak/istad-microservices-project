package kh.edu.istad.platform.payment.domain.service;

import kh.edu.istad.platform.payment.domain.entity.CreditEntry;
import kh.edu.istad.platform.payment.domain.entity.CreditHistory;
import kh.edu.istad.platform.payment.domain.entity.Payment;
import kh.edu.istad.platform.payment.domain.event.PaymentEvent;

import java.time.ZonedDateTime;
import java.util.List;

public interface PaymentDomainService {

    PaymentEvent validateAndInitiatePayment(
            Payment payment,
            CreditEntry creditEntry,
            List<CreditHistory> creditHistories,
            List<String> failureMessage
    );
    PaymentEvent validateAndCancelPayment(
            Payment payment,
            CreditEntry creditEntry,
            List<CreditHistory> creditHistories,
            List<String> failureMessage
    );
}
