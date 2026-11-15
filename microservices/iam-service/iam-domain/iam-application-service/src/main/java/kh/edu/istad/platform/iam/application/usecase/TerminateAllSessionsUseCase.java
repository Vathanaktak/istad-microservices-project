package kh.edu.istad.platform.iam.application.usecase;

import java.time.Instant;
import java.util.UUID;
import kh.edu.istad.platform.iam.application.port.out.AuditLogRepository;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.SessionPort;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.model.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TerminateAllSessionsUseCase {
    private final SessionPort sessionPort;
    private final UserRepository users;
    private final AuditLogRepository auditLogs;
    private final CurrentActorProvider actorProvider;

    public void execute(String principalName) {
        sessionPort.terminateAllSessions(principalName);
        if (principalName != null) {
            users.findByUsernameOrEmail(principalName).ifPresent(u -> {
                try {
                    AuditLog log = new AuditLog();
                    log.setUuid(UUID.randomUUID());
                    log.setUserId(u.getId());
                    log.setCategory("AUTH");
                    log.setAction("SESSION_TERMINATED");
                    log.setOutcome("SUCCESS");
                    log.setActor(u.getUsername());
                    log.setTarget(u.getUsername());
                    log.setIpAddress(actorProvider.currentIpAddress());
                    log.setDetail("All other active sessions terminated");
                    log.setOccurredAt(Instant.now());
                    auditLogs.save(log);
                } catch (Exception ignored) {
                }
            });
        }
    }
}
