package kh.edu.istad.platform.payment.persistence.adapter;

import kh.edu.istad.common.valueobject.CustomerId;
import kh.edu.istad.common.valueobject.Money;
import kh.edu.istad.common.valueobject.OrderId;
import kh.edu.istad.platform.payment.domain.entity.Payment;
import kh.edu.istad.platform.payment.domain.port.out.PaymentRepository;
import kh.edu.istad.platform.payment.domain.valueobject.PaymentId;
import kh.edu.istad.platform.payment.persistence.entity.PaymentJpaEntity;
import kh.edu.istad.platform.payment.persistence.repository.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Payment save(Payment payment) {

        PaymentJpaEntity entity = PaymentJpaEntity.builder()
                .customerId(payment.getCustomerId().value())
                .id(payment.getId().value())
                .orderId(payment.getOrderId().value())
                .price(payment.getPrice().getAmount())
                .status(payment.getPaymentStatus())
                .createAt(payment.getCreatedAt())
                .build();

        PaymentJpaEntity saved = paymentJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {
        return paymentJpaRepository.findByOrderId(orderId)
                .map(this::toDomain);
    }

    private Payment toDomain(PaymentJpaEntity entity) {
        return new Payment(
                new PaymentId(entity.getId()),
                new CustomerId(entity.getCustomerId()),
                new OrderId(entity.getOrderId()),
                new Money(entity.getPrice()),
                entity.getStatus(),
                entity.getCreateAt()
        );
    }
}
