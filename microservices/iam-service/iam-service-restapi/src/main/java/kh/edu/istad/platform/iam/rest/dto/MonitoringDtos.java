package kh.edu.istad.platform.iam.rest.dto;

import java.time.Instant;
import java.util.UUID;

public final class MonitoringDtos {
    private MonitoringDtos() {}

    public record LoginRecordResponse(
        Long id,
        String username,
        String status,
        String ipAddress,
        String location,
        String userAgent,
        String device,
        String browser,
        String os,
        boolean suspicious,
        String failureReason,
        Instant occurredAt
    ) {}

    public record AuditLogResponse(
        Long id,
        String category,
        String action,
        String outcome,
        String actor,
        String target,
        String ipAddress,
        String detail,
        Instant occurredAt
    ) {}

    public record ActivityResponse(
        Long id,
        String type,
        String description,
        String ipAddress,
        Instant occurredAt
    ) {}

    public record DeactivationRequestDto(String reason, String details) {}
    public record DeactivationResponse(String status, Instant requestedAt) {}
    public record DeactivationReviewRequest(String reviewNotes) {}

    public record DeactivationProposalResponse(
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

    public record SessionResponse(
        String id,
        String device,
        boolean current,
        String browser,
        String os,
        String ipAddress,
        String location,
        Instant lastActiveAt,
        Instant createdAt
    ) {}
}
