package kh.edu.istad.platform.order.persistence.adapter;

import kh.edu.istad.platform.business.domain.valueobject.BusinessId;
import kh.edu.istad.platform.business.domain.valueobject.Money;
import kh.edu.istad.platform.order.domain.entity.Order;
import kh.edu.istad.platform.order.domain.entity.OrderItem;
import kh.edu.istad.platform.order.domain.port.out.OrderRepository;
import kh.edu.istad.platform.order.domain.valueobject.OrderId;
import kh.edu.istad.platform.order.domain.valueobject.OrderItemId;
import kh.edu.istad.platform.order.domain.valueobject.StreetAddress;
import kh.edu.istad.platform.order.domain.valueobject.TrackingId;
import kh.edu.istad.platform.order.persistence.entity.OrderItemJpaEntity;
import kh.edu.istad.platform.order.persistence.entity.OrderJpaEntity;
import kh.edu.istad.platform.order.persistence.repository.OrderJapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJapRepository orderJapRepository;

    @Override
    public Order save(Order order) {

        OrderJpaEntity orderJpaEntity = OrderJpaEntity.builder()
                .id(order.getId().value())
                .customerId(order.getCustomerId())
                .businessId(order.getBusinessId().value())
                .trackingId(order.getTrackingId().value())
                .price(order.getPrice().amount())
                .orderStatus(order.getOrderStatus())
                .street(order.getStreetAddress().street())
                .postalCode(order.getStreetAddress().postalCode())
                .city(order.getStreetAddress().city())
                .build();
        orderJpaEntity.setOrderItemJpaEntities(
                order.getItems().stream()
                        .map(item -> OrderItemJpaEntity.builder()
                                .qty(item.getQty())
                                .id(item.getId().value())
                                .productName(item.getProductName())
                                .orderJpaEntity(orderJpaEntity)
                                .subTotal(item.getSubTotal().amount())
                                .price(item.getPrice().amount())
                                .build()
                        ).collect(Collectors.toList())

        );
        OrderJpaEntity saved = orderJapRepository.save(orderJpaEntity);


        return Order.builder()
                .id(new OrderId(saved.getId()))
                .customerId(saved.getCustomerId())
                .businessId(new BusinessId(saved.getBusinessId()))
                .trackingId(new TrackingId(saved.getTrackingId()))
                .money(new Money(saved.getPrice()))
                .orderStatus(saved.getOrderStatus())
                .streetAddress(new StreetAddress(
                        saved.getStreet(),saved.getPostalCode(),saved.getCity()
                ))
                .items(saved.getOrderItemJpaEntities().stream()
                        .map(
                                i -> new OrderItem(new OrderItemId(i.getId()),
                                        i.getProductName(),i.getQty(),new Money(i.getPrice()))

                        ).collect(Collectors.toList())
                )
                .build();

    }
}
