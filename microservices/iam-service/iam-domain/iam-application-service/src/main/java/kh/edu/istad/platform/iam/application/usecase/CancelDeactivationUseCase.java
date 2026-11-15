package kh.edu.istad.platform.iam.application.usecase;

import java.time.Instant;
import java.util.UUID;
import kh.edu.istad.platform.iam.application.port.out.AuditLogRepository;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.DeactivationRequestRepository;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.model.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CancelDeactivationUseCase {
    private final DeactivationRequestRepository repository;
    private final UserRepository users;
    private final AuditLogRepository auditLogs;
    private final CurrentActorProvider actorProvider;

    public void execute(UUID userUuid) {
        users.findByUuid(userUuid).ifPresent(u -> {
            repository.findPendingByUserId(u.getId()).ifPresent(r -> {
                r.setStatus("CANCELLED");
                r.setCancelledAt(Instant.now());
                repository.save(r);

                try {
                    AuditLog log = new AuditLog();
                    log.setUuid(UUID.randomUUID());
                    log.setUserId(u.getId());
                    log.setCategory("ACCOUNT");
                    log.setAction("DEACTIVATION_CANCELLED");
                    log.setOutcome("SUCCESS");
                    log.setActor(u.getUsername());
                    log.setTarget(u.getUsername());
                    log.setIpAddress(actorProvider.currentIpAddress());
                    log.setDetail("Account deactivation request cancelled");
                    log.setOccurredAt(Instant.now());
                    auditLogs.save(log);
                } catch (Exception ignored) {
                }
            });
        });
    }
}
