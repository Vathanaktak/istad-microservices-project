package kh.edu.istad.platform.iam.application.usecase;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Transactional
public class UnlinkIdentityUseCase {
    private final UserAssembler assembler;
    private final UserRepository users;
    private final CurrentActorProvider actor;
    private final AuditLogRepository auditLogs;

    public void execute(UUID userUuid, UUID identityUuid) {
        User u = assembler.require(userUuid);
        List<Identity> mine = users.findIdentities(u.getId());
        Identity target = mine.stream().filter(i -> i.getUuid().equals(identityUuid)).findFirst()
            .orElseThrow(() -> NotFoundException.of("Identity", identityUuid));
        int methods = mine.size() + (u.hasPassword() ? 1 : 0);
        if (methods <= 1) throw new BusinessRuleException("last-sign-in-method", "Cannot unlink the last sign-in method");
        target.softDelete(actor.currentActor());
        users.saveIdentity(target);

        try {
            AuditLog log = new AuditLog();
            log.setUuid(UUID.randomUUID());
            log.setUserId(u.getId());
            log.setCategory("AUTH");
            log.setAction("IDENTITY_UNLINKED");
            log.setOutcome("SUCCESS");
            log.setActor(u.getUsername());
            log.setTarget(target.getProvider() != null ? target.getProvider().name() : "IDENTITY");
            log.setIpAddress(actor.currentIpAddress());
            log.setDetail("Unlinked " + target.getProvider() + " account");
            log.setOccurredAt(Instant.now());
            auditLogs.save(log);
        } catch (Exception ignored) {
        }
    }
}
