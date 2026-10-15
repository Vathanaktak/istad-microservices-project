package kh.edu.istad.platform.payment.persistence.entity;

import jakarta.persistence.*;
import kh.edu.istad.platform.payment.domain.valueobject.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentJpaEntity {

    @Id
    private UUID id;
    private UUID customerId;
    private UUID orderId;
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    private ZonedDateTime createAt;
}
