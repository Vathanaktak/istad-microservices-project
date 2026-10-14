package kh.edu.istad.platform.order.restapi.dto;

import java.util.UUID;

public record CreateOrderResponse(
        UUID trackingId,
        String orderStatus,
        String message
) {
}
