package kh.edu.istad.platform.iam.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "login_history")
@Getter
@Setter
public class LoginHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String status;

    @Column(name = "ip_address")
    private String ipAddress;

    private String location;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    private String device;
    private String browser;
    private String os;

    @Column(nullable = false)
    private boolean suspicious;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt = Instant.now();
}
