package kh.edu.istad.platform.iam.persistence.adapter;

import java.util.Optional;
import kh.edu.istad.platform.iam.application.port.out.PasswordResetTokenRepository;
import kh.edu.istad.platform.iam.domain.model.PasswordResetToken;
import kh.edu.istad.platform.iam.persistence.entity.PasswordResetTokenEntity;
import kh.edu.istad.platform.iam.persistence.repository.PasswordResetTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepository {
    private final PasswordResetTokenJpaRepository jpa;

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        PasswordResetTokenEntity e;
        if (token.getId() == null) {
            e = new PasswordResetTokenEntity();
            e.setUuid(token.getUuid());
        } else {
            e = jpa.findById(token.getId()).orElseThrow();
        }
        e.setUserId(token.getUserId());
        e.setTokenHash(token.getTokenHash());
        e.setExpiresAt(token.getExpiresAt());
        e.setUsed(token.isUsed());
        e.setUsedAt(token.getUsedAt());
        if (token.getCreatedBy() != null) {
            e.setCreatedBy(token.getCreatedBy());
        }
        PasswordResetTokenEntity saved = jpa.saveAndFlush(e);
        return toDomain(saved);
    }

    @Override
    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return jpa.findByTokenHashAndUsedFalse(tokenHash).map(this::toDomain);
    }

    @Override
    public void invalidateTokensForUser(Long userId) {
        jpa.invalidateAllByUserId(userId);
    }

    private PasswordResetToken toDomain(PasswordResetTokenEntity e) {
        PasswordResetToken d = new PasswordResetToken();
        d.setId(e.getId());
        d.setUuid(e.getUuid());
        d.setUserId(e.getUserId());
        d.setTokenHash(e.getTokenHash());
        d.setExpiresAt(e.getExpiresAt());
        d.setUsed(e.isUsed());
        d.setUsedAt(e.getUsedAt());
        d.setCreatedAt(e.getCreatedAt());
        d.setCreatedBy(e.getCreatedBy());
        return d;
    }
}
