//package kh.edu.istad.platform.order.persistence.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.math.BigDecimal;
//import java.util.UUID;
//
//@Entity
//@Table(name = "order_items")
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class OrderItemJpaEntity {
//
//    @Id
//    private UUID id;
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "order_id")
//    private OrderJpaEntity orderJpaEntity;
//    private String productName;
//    private int qty;
//    private BigDecimal price;
//    private BigDecimal subTotal;
//}
