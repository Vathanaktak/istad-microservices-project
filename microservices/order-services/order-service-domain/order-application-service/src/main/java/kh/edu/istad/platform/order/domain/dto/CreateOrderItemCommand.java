package kh.edu.istad.platform.order.domain.dto;

import java.math.BigDecimal;

public record CreateOrderItemCommand(
        String productName,
        int qty,
        BigDecimal price
) {
}
