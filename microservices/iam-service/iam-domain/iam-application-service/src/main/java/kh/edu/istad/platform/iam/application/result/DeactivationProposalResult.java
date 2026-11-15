package kh.edu.istad.platform.iam.application.result;

import java.time.Instant;
import java.util.UUID;

public record DeactivationProposalResult(
    UUID uuid,
    UUID userUuid,
    String username,
    String email,
    String name,
    String reason,
    String details,
    String status,
    Instant requestedAt,
    Instant cancelledAt,
    Instant processedAt,
    String reviewNotes,
    String reviewedBy
) {}
