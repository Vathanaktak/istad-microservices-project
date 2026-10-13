package kh.edu.istad.platform.order.domain.dto;

import java.util.UUID;

public record CreateOrderResult(
        UUID orderTrackingId,
        String orderStatus,
        String message
) {
}
