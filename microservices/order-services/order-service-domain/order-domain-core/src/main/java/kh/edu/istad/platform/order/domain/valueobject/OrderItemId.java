package kh.edu.istad.platform.order.domain.valueobject;

import java.util.UUID;

public record OrderItemId(
        UUID value
) {
    public static OrderItemId generate(){
        return new OrderItemId(UUID.randomUUID());
    }
}
