package kh.edu.istad.platform.iam.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import kh.edu.istad.platform.iam.application.port.out.LoginHistoryRepository;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.application.usecase.RecordLoginAttemptUseCase;
import kh.edu.istad.platform.iam.domain.model.User;
import org.junit.jupiter.api.Test;

class RecordLoginAttemptUseCaseTest {
    private final UserRepository users = mock(UserRepository.class);
    private final LoginHistoryRepository loginHistory = mock(LoginHistoryRepository.class);
    private final RecordLoginAttemptUseCase useCase = new RecordLoginAttemptUseCase(users, loginHistory);

    @Test
    void maxFailedAttemptsSetsLockedUntil() {
        User u = new User();
        u.setId(1L);
        u.setUsername("bob");
        u.setFailedLoginAttempts(4);
        when(users.findByUsernameOrEmail("bob")).thenReturn(Optional.of(u));

        useCase.execute("bob", false, "bad_credentials", "192.168.1.5", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

        assertEquals(5, u.getFailedLoginAttempts());
        assertNotNull(u.getLockedUntil());
        assertTrue(u.isTemporarilyLocked());
        // admin non-locked flag should NOT be altered by failed login throttling
        assertTrue(u.isAccountNonLocked());
        verify(users).save(u);
        verify(loginHistory).save(argThat(r ->
                "FAILED".equals(r.getStatus()) &&
                "bob".equals(r.getUsername()) &&
                "192.168.1.5".equals(r.getIpAddress()) &&
                "Windows".equals(r.getOs())
        ));
    }

    @Test
    void successfulLoginClearsLockout() {
        User u = new User();
        u.setId(1L);
        u.setUsername("bob");
        u.setFailedLoginAttempts(5);
        u.setLockedUntil(java.time.Instant.now().plusSeconds(600));
        when(users.findByUsernameOrEmail("bob")).thenReturn(Optional.of(u));

        useCase.execute("bob", true, null, "127.0.0.1", null);

        assertEquals(0, u.getFailedLoginAttempts());
        assertNull(u.getLockedUntil());
        assertFalse(u.isTemporarilyLocked());
        verify(users).save(u);
        verify(loginHistory).save(argThat(r -> "SUCCESS".equals(r.getStatus()) && "bob".equals(r.getUsername())));
    }
}
