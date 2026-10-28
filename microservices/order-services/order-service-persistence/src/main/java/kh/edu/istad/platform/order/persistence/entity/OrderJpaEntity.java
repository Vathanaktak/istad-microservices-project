//package kh.edu.istad.platform.order.persistence.entity;
//
//import jakarta.persistence.*;
//import kh.edu.istad.platform.order.domain.valueobject.OrderStatus;
//import lombok.*;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.UUID;
//
//@Entity
//@Table(name = "orders")
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class OrderJpaEntity {
//
//    @Id
//    private UUID id;
//    private UUID customerId;
//    private UUID businessId;
//    private UUID trackingId;
//    private BigDecimal price;
//    @Enumerated(EnumType.STRING)
//    private OrderStatus orderStatus;
//    private String street;
//    private String postalCode;
//    private String city;
//    @OneToMany(mappedBy = "orderJpaEntity",cascade = CascadeType.ALL,
//    orphanRemoval = true)
//    private List<OrderItemJpaEntity> orderItemJpaEntities;
//
//}
