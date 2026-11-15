package kh.edu.istad.platform.iam.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRecord {
    private Long id;
    private UUID uuid;
    private Long userId;
    private String username;
    private String status; // "SUCCESS" | "FAILED"
    private String ipAddress;
    private String location;
    private String userAgent;
    private String device;
    private String browser;
    private String os;
    private boolean suspicious;
    private String failureReason;
    private Instant occurredAt;
}
