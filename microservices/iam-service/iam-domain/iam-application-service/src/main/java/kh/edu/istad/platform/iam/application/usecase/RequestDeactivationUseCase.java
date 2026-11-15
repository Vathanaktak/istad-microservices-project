package kh.edu.istad.platform.iam.application.usecase;

import java.time.Instant;
import java.util.UUID;
import kh.edu.istad.platform.iam.application.port.out.AuditLogRepository;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.DeactivationRequestRepository;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
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
public class RequestDeactivationUseCase {
    private final DeactivationRequestRepository repository;
    private final UserRepository users;
    private final AuditLogRepository auditLogs;
    private final CurrentActorProvider actorProvider;

    public DeactivationRequest execute(UUID userUuid, String reason, String details) {
        User u = users.findByUuid(userUuid).orElseThrow(() -> NotFoundException.of("User", userUuid));
        DeactivationRequest req = repository.findPendingByUserId(u.getId()).orElseGet(() -> {
            DeactivationRequest r = new DeactivationRequest();
            r.setUuid(UUID.randomUUID());
            r.setUserId(u.getId());
            r.setStatus("PENDING");
            r.setRequestedAt(Instant.now());
            return r;
        });
        req.setReason(reason);
        req.setDetails(details);
        DeactivationRequest saved = repository.save(req);

        try {
            AuditLog log = new AuditLog();
            log.setUuid(UUID.randomUUID());
            log.setUserId(u.getId());
            log.setCategory("ACCOUNT");
            log.setAction("DEACTIVATION_REQUESTED");
            log.setOutcome("SUCCESS");
            log.setActor(u.getUsername());
            log.setTarget(u.getUsername());
            log.setIpAddress(actorProvider.currentIpAddress());
            log.setDetail("Account deactivation requested: " + reason);
            log.setOccurredAt(Instant.now());
            auditLogs.save(log);
        } catch (Exception ignored) {
        }

        return saved;
    }
}
