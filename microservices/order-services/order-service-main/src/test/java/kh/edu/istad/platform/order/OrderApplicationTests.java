package kh.edu.istad.platform.order;

import kh.edu.istad.platform.order.domain.dto.CreateOrderCommand;
import kh.edu.istad.platform.order.domain.dto.CreateOrderItemCommand;
import kh.edu.istad.platform.order.domain.dto.CreateOrderResult;
import kh.edu.istad.platform.order.domain.usecase.InitiateOrderUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class OrderApplicationTests {

    @Autowired
    private InitiateOrderUseCase initiateOrderUseCase;

    @Test
    void testCreateOrder() {
        CreateOrderCommand command = new CreateOrderCommand(
                UUID.fromString("4e969de6-088a-4d64-b227-02e352cd0d84"),
                UUID.fromString("369748a7-ca14-4e46-85b4-4a0ddec1ca11"),
                BigDecimal.valueOf(50.00),
                "Monivong Blvd",
                "12000",
                "Phnom Penh",
                List.of(new CreateOrderItemCommand("Laptop", 1, BigDecimal.valueOf(50.00)))
        );

        CreateOrderResult result = initiateOrderUseCase.createOrder(command);
        assertNotNull(result);
        assertNotNull(result.orderTrackingId());
        assertEquals("PENDING", result.orderStatus());
        System.out.println("Order created successfully with tracking ID: " + result.orderTrackingId());
    }

}
