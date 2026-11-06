package kh.edu.istad.platform.order.domain.dto.client;

public record QueryProductResult(
        String id,
        String name,
        double price,
        int quantity
) {
}
