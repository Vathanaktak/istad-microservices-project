package kh.edu.istad.platform.iam.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;
import kh.edu.istad.platform.iam.application.command.ResetPasswordWithTokenCommand;
import kh.edu.istad.platform.iam.application.port.out.AuditLogRepository;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.PasswordHasher;
import kh.edu.istad.platform.iam.application.port.out.PasswordResetTokenRepository;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.application.usecase.ResetPasswordUseCase;
import kh.edu.istad.platform.iam.domain.exception.BusinessRuleException;
import kh.edu.istad.platform.iam.domain.model.PasswordResetToken;
import kh.edu.istad.platform.iam.domain.model.User;
import org.junit.jupiter.api.Test;

class ResetPasswordUseCaseTest {
    private final PasswordResetTokenRepository tokens = mock(PasswordResetTokenRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final PasswordHasher hasher = mock(PasswordHasher.class);
    private final AuditLogRepository auditLogs = mock(AuditLogRepository.class);
    private final CurrentActorProvider actorProvider = () -> "tester";
    private final ResetPasswordUseCase useCase = new ResetPasswordUseCase(tokens, users, hasher, auditLogs, actorProvider);

    @Test
    void invalidTokenThrows() {
        when(tokens.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThrows(BusinessRuleException.class, () ->
            useCase.execute(new ResetPasswordWithTokenCommand("bad-token", "NewP@ssw0rd!")));
        verify(users, never()).save(any());
    }

    @Test
    void validTokenChangesPasswordAndMarksUsed() {
        PasswordResetToken token = new PasswordResetToken();
        token.setUserId(10L);
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        token.setUsed(false);

        User u = new User();
        u.setId(10L);
        u.setEmail("user@example.com");

        when(tokens.findByTokenHash(anyString())).thenReturn(Optional.of(token));
        when(users.findById(10L)).thenReturn(Optional.of(u));
        when(hasher.hash("NewP@ssw0rd!")).thenReturn("{bcrypt}newhash");

        useCase.execute(new ResetPasswordWithTokenCommand("valid-token", "NewP@ssw0rd!"));

        assertEquals("{bcrypt}newhash", u.getPasswordHash());
        assertTrue(token.isUsed());
        verify(users).save(u);
        verify(tokens).save(token);
        verify(tokens).invalidateTokensForUser(10L);
    }
}
