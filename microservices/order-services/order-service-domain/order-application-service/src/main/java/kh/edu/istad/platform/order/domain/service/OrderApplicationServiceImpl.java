package kh.edu.istad.platform.order.domain.service;

import kh.edu.istad.common.exception.DomainException;
import kh.edu.istad.platform.business.domain.valueobject.BusinessId;
import kh.edu.istad.platform.business.domain.valueobject.Money;
import kh.edu.istad.platform.order.domain.dto.CreateOrderCommand;
import kh.edu.istad.platform.order.domain.dto.CreateOrderResult;
import kh.edu.istad.platform.order.domain.entity.Order;
import kh.edu.istad.platform.order.domain.entity.OrderItem;
import kh.edu.istad.platform.order.domain.event.OrderCreatedEvent;
import kh.edu.istad.platform.order.domain.exception.ServiceUnavailableException;
import kh.edu.istad.platform.order.domain.port.out.BusinessClient;
import kh.edu.istad.platform.order.domain.port.out.CustomerClient;
import kh.edu.istad.platform.order.domain.port.out.OrderRepository;
import kh.edu.istad.platform.order.domain.port.out.PaymentClient;
import kh.edu.istad.platform.order.domain.usecase.InitiateOrderUseCase;
import kh.edu.istad.platform.order.domain.valueobject.StreetAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderApplicationServiceImpl implements InitiateOrderUseCase {

    private final OrderDomainService orderDomainService;
    private final OrderRepository orderRepository;
    private final BusinessClient businessClient;
    private final CustomerClient customerClient;
    private final PaymentClient paymentClient;

    @Override
    public CreateOrderResult createOrder(CreateOrderCommand command) {

        // 1) Ask customer-service: does the customer exist and is it active?
        CustomerClient.CustomerInfo customer = customerClient.getCustomer(command.customerId())
                .orElseThrow(() -> new DomainException("Customer not found: " + command.customerId()));
        if (!customer.active()) {
            throw new DomainException("Customer is not active: " + command.customerId());
        }

        BusinessClient.BusinessInfo business = businessClient.getBusiness(command.businessId())
                .orElseThrow(() -> new DomainException("Business not found: " + command.businessId()));
        if (!business.active()) {
            throw new DomainException("Business is not active: " + command.businessId());
        }

        // 3) Build and save the order as PENDING (existing logic)
        Order order = Order.builder()
                .customerId(command.customerId())
                .businessId(new BusinessId(command.businessId()))
                .streetAddress(new StreetAddress(command.street(),
                        command.postalCode(), command.city()))
                .money(new Money(command.price()))
                .items(command.itemCommands().stream()
                        .map(i -> new OrderItem(
                                null, i.productName(), i.qty(), new Money(i.price())
                        ))
                        .collect(Collectors.toList())
                ).build();
        OrderCreatedEvent event = orderDomainService.validateAndInitiateOrder(order);
        Order savedOrder = orderRepository.save(event.order());
        log.info("Order {} saved as PENDING", savedOrder.getTrackingId().value());

        // 4) Ask payment-service to charge the customer
        PaymentClient.PaymentInfo payment;
        try {
            payment = paymentClient.pay(
                    savedOrder.getId().value(), command.customerId(), command.price());
        } catch (ServiceUnavailableException ex) {
            // payment-service is down: do not leave a PENDING order behind
            savedOrder.cancel(List.of(ex.getMessage()));
            orderRepository.save(savedOrder);
            log.error("Order {} cancelled because {}", savedOrder.getTrackingId().value(), ex.getMessage());
            throw ex;
        }

        // 5) PAID or CANCELLED
        String message;
        if (payment.isCompleted()) {
            savedOrder.pay();
            message = "Order created and paid successfully";
        } else {
            savedOrder.cancel(payment.failureMessages());
            message = "Order cancelled, payment failed: " + payment.failureMessages();
        }
        Order finalOrder = orderRepository.save(savedOrder);
        log.info("Order {} is now {}", finalOrder.getTrackingId().value(), finalOrder.getOrderStatus());

        return new CreateOrderResult(
                finalOrder.getTrackingId().value(),
                finalOrder.getOrderStatus().name(),
                message
        );
    }
}
