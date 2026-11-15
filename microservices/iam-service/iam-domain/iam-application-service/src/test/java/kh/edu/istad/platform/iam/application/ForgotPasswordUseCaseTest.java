package kh.edu.istad.platform.iam.application;

import static org.mockito.Mockito.*;

import java.util.Optional;
import kh.edu.istad.platform.iam.application.command.ForgotPasswordCommand;
import kh.edu.istad.platform.iam.application.port.out.PasswordResetNotifier;
import kh.edu.istad.platform.iam.application.port.out.PasswordResetTokenRepository;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.application.usecase.ForgotPasswordUseCase;
import kh.edu.istad.platform.iam.domain.model.User;
import org.junit.jupiter.api.Test;

class ForgotPasswordUseCaseTest {
    private final UserRepository users = mock(UserRepository.class);
    private final PasswordResetTokenRepository tokens = mock(PasswordResetTokenRepository.class);
    private final PasswordResetNotifier notifier = mock(PasswordResetNotifier.class);
    private final ForgotPasswordUseCase useCase = new ForgotPasswordUseCase(users, tokens, notifier);

    @Test
    void nonExistentEmailDoesNotThrowOrSend() {
        when(users.findByUsernameOrEmail("ghost@example.com")).thenReturn(Optional.empty());

        useCase.execute(new ForgotPasswordCommand("ghost@example.com"));

        verify(tokens, never()).save(any());
        verify(notifier, never()).notifyPasswordReset(any(), any());
    }

    @Test
    void validEmailCreatesTokenAndNotifies() {
        User u = new User();
        u.setId(42L);
        u.setEmail("alice@example.com");
        u.setEnabled(true);
        when(users.findByUsernameOrEmail("alice@example.com")).thenReturn(Optional.of(u));

        useCase.execute(new ForgotPasswordCommand("alice@example.com"));

        verify(tokens).invalidateTokensForUser(42L);
        verify(tokens).save(any());
        verify(notifier).notifyPasswordReset(eq("alice@example.com"), anyString());
    }
}
