package kh.edu.istad.platform.iam.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditLog {
    private Long id;
    private UUID uuid;
    private Long userId;
    private String category;
    private String action;
    private String outcome;
    private String actor;
    private String target;
    private String ipAddress;
    private String detail;
    private Instant occurredAt;
}
