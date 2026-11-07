package kh.edu.istad.platform.iam.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import kh.edu.istad.platform.iam.application.port.out.*;
import kh.edu.istad.platform.iam.application.support.UserAssembler;
import kh.edu.istad.platform.iam.application.usecase.UnlinkIdentityUseCase;
import kh.edu.istad.platform.iam.domain.exception.BusinessRuleException;
import kh.edu.istad.platform.iam.domain.model.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class UnlinkIdentityUseCaseTest {
    private final UserRepository users = mock(UserRepository.class);
    private final UserAssembler assembler = new UserAssembler(users, mock(RoleRepository.class));
    private final CurrentActorProvider actor = () -> "tester";
    private final UnlinkIdentityUseCase useCase = new UnlinkIdentityUseCase(assembler, users, actor);

    private User user(boolean password) {
        User u = new User(); u.setId(1L);
        if (password) u.setPasswordHash("{bcrypt}x");
        when(users.findByUuid(u.getUuid())).thenReturn(java.util.Optional.of(u));
        return u;
    }
    private Identity identity() { Identity i = new Identity(); i.setUserId(1L); i.setProvider(IdentityProvider.GOOGLE); return i; }

    @Test
    void cannot_unlink_last_sign_in_method() {
        User u = user(false);
        Identity i = identity();
        when(users.findIdentities(1L)).thenReturn(List.of(i));
        assertThrows(BusinessRuleException.class, () -> useCase.execute(u.getUuid(), i.getUuid()));
        verify(users, never()).saveIdentity(any());
    }

    @Test
    void can_unlink_when_password_exists() {
        User u = user(true);
        Identity i = identity();
        when(users.findIdentities(1L)).thenReturn(List.of(i));
        useCase.execute(u.getUuid(), i.getUuid());
        assertTrue(i.isDeleted());
        verify(users).saveIdentity(i);
    }
}
