package kh.edu.istad.platform.order.domain.valueobject;

import java.util.UUID;

public record OrderItemId(
        Integer value
) {
    public static OrderItemId generate(){
        return new OrderItemId(Integer.valueOf(String.valueOf(UUID.randomUUID())));
    }
}
