package kh.edu.istad.platform.order.domain.event;

import kh.edu.istad.common.event.DomainEvent;
import kh.edu.istad.platform.order.domain.entity.Order;

import java.time.ZonedDateTime;

public record OrderCancelledEvent(
        Order order,
        ZonedDateTime createAt
) implements DomainEvent<Order> {
}
