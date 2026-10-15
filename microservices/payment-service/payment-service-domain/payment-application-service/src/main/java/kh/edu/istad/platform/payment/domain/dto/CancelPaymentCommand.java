package kh.edu.istad.platform.payment.domain.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CancelPaymentCommand(
        @NotNull
        UUID orderId
) {
}
