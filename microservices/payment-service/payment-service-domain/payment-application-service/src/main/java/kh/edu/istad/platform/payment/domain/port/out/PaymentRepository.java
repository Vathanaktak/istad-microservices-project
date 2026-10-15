package kh.edu.istad.platform.payment.domain.port.out;

import kh.edu.istad.platform.payment.domain.entity.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    Payment save(Payment payment);
    Optional<Payment> findByOrderId(UUID orderId);
}
