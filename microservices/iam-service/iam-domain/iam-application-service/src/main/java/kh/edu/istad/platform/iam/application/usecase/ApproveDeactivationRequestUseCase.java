package kh.edu.istad.platform.iam.application.usecase;

import java.time.Instant;
import java.util.UUID;
import kh.edu.istad.platform.iam.application.port.out.AuditLogRepository;
import kh.edu.istad.platform.iam.application.port.out.DeactivationRequestRepository;
import kh.edu.istad.platform.iam.application.port.out.SessionPort;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.application.result.DeactivationProposalResult;
import kh.edu.istad.platform.iam.domain.exception.ConflictException;
import kh.edu.istad.platform.iam.domain.exception.NotFoundException;
import kh.edu.istad.platform.iam.domain.model.AuditLog;
import kh.edu.istad.platform.iam.domain.model.DeactivationRequest;
import kh.edu.istad.platform.iam.domain.model.User;
import kh.edu.istad.platform.iam.domain.service.IamDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ApproveDeactivationRequestUseCase {
    private final DeactivationRequestRepository repository;
    private final UserRepository users;
    private final IamDomainService iamDomainService;
    private final SessionPort sessionPort;
    private final AuditLogRepository auditLogs;

    public DeactivationProposalResult execute(UUID requestUuid, String reviewNotes, String adminUsername) {
        DeactivationRequest req = repository.findByUuid(requestUuid)
                .orElseThrow(() -> NotFoundException.of("DeactivationRequest", requestUuid));

        if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
            throw new ConflictException("DEACTIVATION_REQUEST_NOT_PENDING", "Deactivation request is already " + req.getStatus());
        }

        User u = users.findById(req.getUserId())
                .orElseThrow(() -> NotFoundException.of("User", req.getUserId()));

        // 1. Disable the user account
        if (u.isEnabled()) {
            iamDomainService.disableUser(u);
            users.save(u);
        }

        // 2. Terminate all active sessions for the user
        try {
            sessionPort.terminateAllSessions(u.getUsername());
        } catch (Exception ignored) {
            // Non-critical if sessions cannot be terminated
        }

        // 3. Update request status
        req.setStatus("PROCESSED");
        req.setProcessedAt(Instant.now());
        req.setReviewNotes(reviewNotes);
        req.setReviewedBy(adminUsername);
        DeactivationRequest saved = repository.save(req);

        // 4. Record audit log
        try {
            AuditLog log = new AuditLog();
            log.setUuid(UUID.randomUUID());
            log.setUserId(u.getId());
            log.setCategory("USER");
            log.setAction("DEACTIVATION_APPROVED");
            log.setOutcome("SUCCESS");
            log.setActor(adminUsername != null ? adminUsername : "admin");
            log.setTarget(u.getUsername());
            log.setDetail("Deactivation request approved. Reason: " + req.getReason() +
                    (reviewNotes != null && !reviewNotes.isBlank() ? " | Notes: " + reviewNotes : ""));
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
