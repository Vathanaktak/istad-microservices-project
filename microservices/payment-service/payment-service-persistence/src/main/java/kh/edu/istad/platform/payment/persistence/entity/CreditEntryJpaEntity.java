package kh.edu.istad.platform.payment.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "credit_entries")
public class CreditEntryJpaEntity {
    @Id
    private UUID id;
    private UUID customerId;
    private BigDecimal totalCreditAmount;
}