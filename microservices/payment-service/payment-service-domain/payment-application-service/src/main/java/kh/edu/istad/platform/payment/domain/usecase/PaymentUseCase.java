package kh.edu.istad.platform.payment.domain.usecase;

import kh.edu.istad.platform.payment.domain.dto.CancelPaymentCommand;
import kh.edu.istad.platform.payment.domain.dto.CreatePaymentCommand;
import kh.edu.istad.platform.payment.domain.dto.PaymentResult;

public interface PaymentUseCase {

    PaymentResult processPayment(CreatePaymentCommand command);
    PaymentResult cancelPayment(CancelPaymentCommand command);
}
