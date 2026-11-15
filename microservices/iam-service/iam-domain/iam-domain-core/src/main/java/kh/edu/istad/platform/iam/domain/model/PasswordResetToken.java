package kh.edu.istad.platform.iam.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetToken {
    private Long id;
    private UUID uuid;
    private Long userId;
    private String tokenHash;
    private Instant expiresAt;
    private boolean used;
    private Instant usedAt;
    private Instant createdAt;
    private String createdBy;

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }

    public void markUsed() {
        this.used = true;
        this.usedAt = Instant.now();
    }
}
