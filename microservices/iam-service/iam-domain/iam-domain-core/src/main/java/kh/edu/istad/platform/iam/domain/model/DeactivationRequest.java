package kh.edu.istad.platform.iam.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeactivationRequest {
    private Long id;
    private UUID uuid;
    private Long userId;
    private String reason;
    private String details;
    private String status; // "PENDING", "CANCELLED", "PROCESSED", "REJECTED"
    private Instant requestedAt;
    private Instant cancelledAt;
    private Instant processedAt;
    private String reviewNotes;
    private String reviewedBy;
}
