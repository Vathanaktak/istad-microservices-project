package kh.edu.istad.platform.payment.persistence.entity;

import jakarta.persistence.*;
import kh.edu.istad.platform.payment.domain.valueobject.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "credit_histories")
public class CreditHistoryJpaEntity {
    @Id
    private UUID id;
    private UUID customerId;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;
}