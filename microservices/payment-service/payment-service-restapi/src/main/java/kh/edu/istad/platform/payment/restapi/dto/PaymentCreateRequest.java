package kh.edu.istad.platform.payment.restapi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCreateRequest(
        @NotNull
        UUID orderId,
        @NotNull
        UUID customerId,
        @NotNull @Positive
        BigDecimal price
) {
}
