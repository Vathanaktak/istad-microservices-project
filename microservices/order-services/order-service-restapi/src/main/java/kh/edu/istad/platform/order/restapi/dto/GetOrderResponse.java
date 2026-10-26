package kh.edu.istad.platform.order.restapi.dto;

import java.util.UUID;

public record GetOrderResponse(
        UUID customerId,
        String username,
        String familyName,
        String givenName,
        String email,
        String phoneNumber
) {
}
