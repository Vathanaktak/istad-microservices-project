package kh.edu.istad.platform.payment.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentCommand(
        @NotNull
        UUID customerId,
        @NotNull
        UUID orderId,
        @NotNull
        @Positive
        BigDecimal
        price
) {
}
