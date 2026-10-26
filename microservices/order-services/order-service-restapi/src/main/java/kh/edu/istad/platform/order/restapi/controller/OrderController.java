package kh.edu.istad.platform.order.restapi.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.order.domain.dto.CreateOrderCommand;
import kh.edu.istad.platform.order.domain.dto.CreateOrderItemCommand;
import kh.edu.istad.platform.order.domain.dto.CreateOrderResult;
import kh.edu.istad.platform.order.domain.port.out.client.CustomerServiceClient;
import kh.edu.istad.platform.order.domain.usecase.InitiateOrderUseCase;
import kh.edu.istad.platform.order.restapi.dto.CreateOrderRequest;
import kh.edu.istad.platform.order.restapi.dto.CreateOrderResponse;
import kh.edu.istad.platform.order.restapi.dto.GetOrderResponse;
import kh.edu.istad.platform.order.restapi.mapper.CustomerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final InitiateOrderUseCase orderUseCase;
    private final CustomerServiceClient customerServiceClient;
    private final CustomerMapper customerMapper;

    @GetMapping("/customer")
    public Flux<GetOrderResponse> getCustomer(){
        return customerServiceClient.getCustomerName()
                .map(customerMapper::toResponse);
    }

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrderResponseResponseEntity(
            @Valid @RequestBody CreateOrderRequest request
            ){

        CreateOrderCommand command = new CreateOrderCommand(
                request.customerId(),
                request.businessId(),
                request.price(),
                request.street(),
                request.postalCode(),
                request.city(),
                request.items().stream()
                        .map(i -> new CreateOrderItemCommand(
                                i.productName(),i.qty(),i.price()
                        ))
                        .collect(Collectors.toList())

        );
        CreateOrderResult result = orderUseCase.createOrder(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        new CreateOrderResponse(result.orderTrackingId(),
                                result.orderStatus(),result.message())
                );

    }

}
