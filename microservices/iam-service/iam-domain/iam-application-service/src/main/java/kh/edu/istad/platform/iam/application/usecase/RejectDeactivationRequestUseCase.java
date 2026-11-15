package kh.edu.istad.platform.iam.application.usecase;

import java.time.Instant;
import java.util.UUID;
import kh.edu.istad.platform.iam.application.port.out.AuditLogRepository;
import kh.edu.istad.platform.iam.application.port.out.DeactivationRequestRepository;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.application.result.DeactivationProposalResult;
import kh.edu.istad.platform.iam.domain.exception.ConflictException;
import kh.edu.istad.platform.iam.domain.exception.NotFoundException;
import kh.edu.istad.platform.iam.domain.model.AuditLog;
import kh.edu.istad.platform.iam.domain.model.DeactivationRequest;
import kh.edu.istad.platform.iam.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RejectDeactivationRequestUseCase {
    private final DeactivationRequestRepository repository;
    private final UserRepository users;
    private final AuditLogRepository auditLogs;

    public DeactivationProposalResult execute(UUID requestUuid, String reviewNotes, String adminUsername) {
        DeactivationRequest req = repository.findByUuid(requestUuid)
                .orElseThrow(() -> NotFoundException.of("DeactivationRequest", requestUuid));

        if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
            throw new ConflictException("DEACTIVATION_REQUEST_NOT_PENDING", "Deactivation request is already " + req.getStatus());
        }

        User u = users.findById(req.getUserId())
                .orElseThrow(() -> NotFoundException.of("User", req.getUserId()));

        req.setStatus("REJECTED");
        req.setProcessedAt(Instant.now());
        req.setReviewNotes(reviewNotes);
        req.setReviewedBy(adminUsername);
        DeactivationRequest saved = repository.save(req);

        // Record audit log
        try {
            AuditLog log = new AuditLog();
            log.setUuid(UUID.randomUUID());
            log.setUserId(u.getId());
            log.setCategory("USER");
            log.setAction("DEACTIVATION_REJECTED");
            log.setOutcome("SUCCESS");
            log.setActor(adminUsername != null ? adminUsername : "admin");
            log.setTarget(u.getUsername());
            log.setDetail("Deactivation request rejected." +
                    (reviewNotes != null && !reviewNotes.isBlank() ? " Reason: " + reviewNotes : ""));
            log.setOccurredAt(Instant.now());
            auditLogs.save(log);
        } catch (Exception ignored) {
        }

        return new DeactivationProposalResult(
                saved.getUuid(),
                u.getUuid(),
                u.getUsername(),
                u.getEmail(),
                u.getName() != null ? u.getName().fullName() : u.getUsername(),
                saved.getReason(),
                saved.getDetails(),
                saved.getStatus(),
                saved.getRequestedAt(),
                saved.getCancelledAt(),
                saved.getProcessedAt(),
                saved.getReviewNotes(),
                saved.getReviewedBy()
        );
    }
}
