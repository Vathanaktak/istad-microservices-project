package kh.edu.istad.platform.order.domain.valueobject;

import java.util.UUID;

public record OrderId(
        UUID value
) {
    public static OrderId generate(){
        return new OrderId(UUID.randomUUID());
    }
}
