package kh.edu.istad.platform.iam.application.usecase;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import kh.edu.istad.platform.iam.application.command.ForgotPasswordCommand;
import kh.edu.istad.platform.iam.application.port.out.PasswordResetNotifier;
import kh.edu.istad.platform.iam.application.port.out.PasswordResetTokenRepository;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.model.PasswordResetToken;
import kh.edu.istad.platform.iam.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ForgotPasswordUseCase {
    public static final Duration TOKEN_VALIDITY = Duration.ofHours(2);
    private final UserRepository users;
    private final PasswordResetTokenRepository tokens;
    private final PasswordResetNotifier notifier;

    public void execute(ForgotPasswordCommand cmd) {
        if (cmd.email() == null || cmd.email().isBlank()) {
            return;
        }
        users.findByUsernameOrEmail(cmd.email().trim().toLowerCase(Locale.ROOT)).ifPresent(user -> {
            if (!user.isEnabled() || user.isDeleted()) {
                return;
            }
            tokens.invalidateTokensForUser(user.getId());

            String rawToken = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
            String hash = hashToken(rawToken);

            PasswordResetToken t = new PasswordResetToken();
            t.setUuid(UUID.randomUUID());
            t.setUserId(user.getId());
            t.setTokenHash(hash);
            t.setExpiresAt(Instant.now().plus(TOKEN_VALIDITY));
            t.setUsed(false);
            t.setCreatedAt(Instant.now());
            t.setCreatedBy("system");

            tokens.save(t);
            notifier.notifyPasswordReset(user.getEmail(), rawToken);
        });
    }

    public static String hashToken(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
