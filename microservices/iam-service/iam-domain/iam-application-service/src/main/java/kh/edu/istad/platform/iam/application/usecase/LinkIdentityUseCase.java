package kh.edu.istad.platform.iam.application.usecase;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Transactional
public class LinkIdentityUseCase {
    private final UserAssembler assembler;
    private final UserRepository users;
    private final AuditLogRepository auditLogs;
    private final CurrentActorProvider actor;

    public IdentityResult execute(UUID userUuid, FederatedIdentityCommand c) {
        User u = assembler.require(userUuid);
        Optional<Identity> taken = users.findIdentity(c.provider(), c.providerSubject());
        if (taken.isPresent() && !taken.get().getUserId().equals(u.getId()))
            throw new ConflictException("identity-linked-elsewhere", "This account is already linked to another user");
        List<Identity> mine = users.findIdentities(u.getId());
        Optional<Identity> sameProvider = mine.stream().filter(i -> i.getProvider() == c.provider()).findFirst();
        if (sameProvider.isPresent() && !sameProvider.get().getProviderSubject().equals(c.providerSubject()))
            throw new ConflictException("provider-already-linked", "One identity per provider per user");
        Identity i = sameProvider.orElseGet(Identity::new);
        i.setUserId(u.getId()); i.setProvider(c.provider()); i.setProviderSubject(c.providerSubject());
        i.setEmail(c.email()); i.setEmailVerified(c.emailVerified()); i.setDisplayName(c.displayName());
        i.setUsername(c.username()); i.setPictureUrl(c.pictureUrl()); i.setAttributes(c.attributes());
        i.recordLogin();
        Identity saved = users.saveIdentity(i);

        try {
            AuditLog log = new AuditLog();
            log.setUuid(UUID.randomUUID());
            log.setUserId(u.getId());
            log.setCategory("AUTH");
            log.setAction("IDENTITY_LINKED");
            log.setOutcome("SUCCESS");
            log.setActor(u.getUsername());
            log.setTarget(c.provider() != null ? c.provider().name() : "IDENTITY");
            log.setIpAddress(actor.currentIpAddress());
            log.setDetail("Linked " + c.provider() + " account" + (c.displayName() != null ? " (" + c.displayName() + ")" : ""));
            log.setOccurredAt(Instant.now());
            auditLogs.save(log);
        } catch (Exception ignored) {
        }

        return UserMapper.toIdentity(saved);
    }
}
