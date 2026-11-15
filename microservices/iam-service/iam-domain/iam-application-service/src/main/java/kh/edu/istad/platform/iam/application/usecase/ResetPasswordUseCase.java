package kh.edu.istad.platform.iam.application.usecase;

import java.time.Instant;
import java.util.UUID;
import kh.edu.istad.platform.iam.application.command.ResetPasswordWithTokenCommand;
import kh.edu.istad.platform.iam.application.port.out.AuditLogRepository;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.PasswordHasher;
import kh.edu.istad.platform.iam.application.port.out.PasswordResetTokenRepository;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.exception.BusinessRuleException;
import kh.edu.istad.platform.iam.domain.exception.NotFoundException;
import kh.edu.istad.platform.iam.domain.model.AuditLog;
import kh.edu.istad.platform.iam.domain.model.PasswordResetToken;
import kh.edu.istad.platform.iam.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ResetPasswordUseCase {
    private final PasswordResetTokenRepository tokens;
    private final UserRepository users;
    private final PasswordHasher hasher;
    private final AuditLogRepository auditLogs;
    private final CurrentActorProvider actorProvider;

    public void execute(ResetPasswordWithTokenCommand cmd) {
        if (cmd.token() == null || cmd.token().isBlank()) {
            throw new BusinessRuleException("invalid-reset-token", "Password reset token is required");
        }
        String hash = ForgotPasswordUseCase.hashToken(cmd.token().trim());
        PasswordResetToken token = tokens.findByTokenHash(hash)
            .filter(PasswordResetToken::isValid)
            .orElseThrow(() -> new BusinessRuleException("invalid-reset-token", "Password reset token is invalid or has expired"));

        User user = users.findById(token.getUserId())
            .orElseThrow(() -> NotFoundException.of("User", token.getUserId()));

        user.changePassword(hasher.hash(cmd.newPassword()));
        users.save(user);

        token.markUsed();
        tokens.save(token);
        tokens.invalidateTokensForUser(user.getId());

        try {
            AuditLog log = new AuditLog();
            log.setUuid(UUID.randomUUID());
            log.setUserId(user.getId());
            log.setCategory("AUTH");
            log.setAction("PASSWORD_CHANGED");
            log.setOutcome("SUCCESS");
            log.setActor(user.getUsername());
            log.setTarget(user.getUsername());
            log.setIpAddress(actorProvider.currentIpAddress());
            log.setDetail("Password reset via email token");
            log.setOccurredAt(Instant.now());
            auditLogs.save(log);
        } catch (Exception ignored) {
        }
    }
}
