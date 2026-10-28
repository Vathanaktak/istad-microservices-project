//package kh.edu.istad.platform.order.domain.service;
//
//import kh.edu.istad.platform.business.domain.valueobject.BusinessId;
//import kh.edu.istad.platform.business.domain.valueobject.Money;
//import kh.edu.istad.platform.order.domain.dto.CreateOrderCommand;
//import kh.edu.istad.platform.order.domain.dto.CreateOrderResult;
//import kh.edu.istad.platform.order.domain.entity.Order;
//import kh.edu.istad.platform.order.domain.entity.OrderItem;
//import kh.edu.istad.platform.order.domain.event.OrderCreatedEvent;
//import kh.edu.istad.platform.order.domain.port.out.OrderRepository;
//import kh.edu.istad.platform.order.domain.usecase.InitiateOrderUseCase;
//import kh.edu.istad.platform.order.domain.valueobject.StreetAddress;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.util.stream.Collectors;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class OrderApplicationServiceImpl implements InitiateOrderUseCase {
//
//    private final OrderDomainService orderDomainService;
////    private final OrderRepository orderRepository;
//
//    @Override
//    public CreateOrderResult createOrder(CreateOrderCommand command) {
//
//        Order order = Order.builder()
//                .customerId(command.customerId())
//                .businessId(new BusinessId(command.businessId()))
//                .streetAddress(new StreetAddress(command.street(),
//                        command.postalCode(), command.city()))
//                .money(new Money(command.price()))
//                .items(command.itemCommands().stream()
//                        .map(i -> new OrderItem(
//                                null,i.productName(), i.qty(), new Money(i.price())
//                        ))
//                        .collect(Collectors.toList())
//                ).build();
//        OrderCreatedEvent event = orderDomainService.validateAndInitiateOrder(order);
//        Order savedOrder = orderRepository.save(event.order());
//
//        log.info("Order created successfully with tracking ID: {}", savedOrder.getTrackingId().value());
//
//
//        return new CreateOrderResult(
//                savedOrder.getTrackingId().value(),
//                savedOrder.getOrderStatus().name(),
//                "Order created Successfully"
//        );
//    }
//}
