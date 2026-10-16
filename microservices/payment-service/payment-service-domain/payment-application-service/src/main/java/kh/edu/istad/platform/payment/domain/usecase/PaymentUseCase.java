package kh.edu.istad.platform.payment.domain.usecase;

import kh.edu.istad.platform.payment.domain.dto.CancelPaymentCommand;
import kh.edu.istad.platform.payment.domain.dto.CreatePaymentCommand;
import kh.edu.istad.platform.payment.domain.dto.PaymentResult;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentUseCase {

    PaymentResult processPayment(CreatePaymentCommand command);
    PaymentResult cancelPayment(CancelPaymentCommand command);
    void addCredit(UUID customerId, BigDecimal amount);
}
