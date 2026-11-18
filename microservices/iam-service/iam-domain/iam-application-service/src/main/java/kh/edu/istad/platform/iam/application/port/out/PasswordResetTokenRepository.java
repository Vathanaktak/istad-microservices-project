package kh.edu.istad.platform.iam.application.port.out;

import java.util.Optional;
import kh.edu.istad.platform.iam.domain.model.PasswordResetToken;

public interface PasswordResetTokenRepository {
    PasswordResetToken save(PasswordResetToken token);
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    void invalidateTokensForUser(Long userId);
}
