package kh.edu.istad.platform.order.restapi.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull
        UUID customerId,
        @NotNull
        UUID businessId,
        @NotNull @DecimalMin("0")
        BigDecimal price,
        @NotBlank
        String street,
        @NotBlank
        String postalCode,
        @NotBlank
        String city,
        @NotEmpty
        List<OrderItemRequest> items
) {
}
