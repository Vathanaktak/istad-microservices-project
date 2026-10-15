package kh.edu.istad.platform.payment.domain.dto;

import kh.edu.istad.platform.payment.domain.valueobject.PaymentStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public record PaymentResult(
        UUID paymentId,
        UUID orderId,
        UUID customerId,
        BigDecimal price,
        PaymentStatus status,
        ZonedDateTime createAt,
        List<String> failureMessages
) {
}
