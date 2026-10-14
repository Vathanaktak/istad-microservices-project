package kh.edu.istad.platform.order.restapi.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrderItemRequest(
        @NotBlank
        String productName,
        @Min(1)
        int qty,
        @NotNull
        @DecimalMin("0")
        BigDecimal price
) {
}
