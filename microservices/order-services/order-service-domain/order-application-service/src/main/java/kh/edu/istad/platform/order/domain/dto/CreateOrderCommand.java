package kh.edu.istad.platform.order.domain.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderCommand(
        UUID customerId,
        UUID businessId,
        BigDecimal price,
        String street,
        String postalCode,
        String city,
        List<CreateOrderItemCommand> itemCommands
) {
}
