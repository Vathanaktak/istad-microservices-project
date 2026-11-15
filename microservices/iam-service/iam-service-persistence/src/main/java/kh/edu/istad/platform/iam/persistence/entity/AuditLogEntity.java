package kh.edu.istad.platform.iam.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
public class AuditLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 30)
    private String category;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(nullable = false, length = 20)
    private String outcome;

    @Column(nullable = false, length = 100)
    private String actor;

    @Column(length = 100)
    private String target;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt = Instant.now();
}
