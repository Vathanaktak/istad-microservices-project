package kh.edu.istad.platform.iam.domain.model;

import kh.edu.istad.platform.iam.domain.exception.BusinessRuleException;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/** Audited link row. Assignments are revoked, never deleted. */
@Getter
@Setter
public abstract class Assignment {
    private Long id;
    private Instant assignedAt;
    private String assignedBy;
    private Instant expiresAt;
    private Instant revokedAt;
    private String revokedBy;
    private String reason;

    protected void init(String by, Instant expiresAt, String reason) {
        this.assignedAt = Instant.now();
        this.assignedBy = by;
        this.expiresAt = expiresAt;
        this.reason = reason;
    }
    public boolean isActive(Instant now) {
        return revokedAt == null && (expiresAt == null || expiresAt.isAfter(now));
    }
    public void revoke(String by, String reason) {
        if (revokedAt != null) throw new BusinessRuleException("already-revoked", "Assignment already revoked");
        this.revokedAt = Instant.now();
        this.revokedBy = by;
        if (reason != null) this.reason = reason;
    }
}
