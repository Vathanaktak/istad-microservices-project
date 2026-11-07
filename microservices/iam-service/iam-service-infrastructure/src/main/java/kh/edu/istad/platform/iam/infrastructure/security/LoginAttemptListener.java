package kh.edu.istad.platform.iam.infrastructure.security;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.usecase.*; import kh.edu.istad.platform.iam.domain.model.IdentityProvider; import kh.edu.istad.platform.iam.domain.exception.*; import lombok.RequiredArgsConstructor; import java.util.*;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor
public class LoginAttemptListener {
    private static final Logger log = LoggerFactory.getLogger(LoginAttemptListener.class);
    private final RecordLoginAttemptUseCase recordAttempt;

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent e) {
        if (e.getAuthentication() instanceof UsernamePasswordAuthenticationToken) safe(e.getAuthentication().getName(), true);
    }
    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent e) {
        if (e.getAuthentication() instanceof UsernamePasswordAuthenticationToken) safe(e.getAuthentication().getName(), false);
    }
    private void safe(String name, boolean ok) {
        try { if (name != null && !name.isBlank()) recordAttempt.execute(name, ok); }
        catch (RuntimeException ex) { log.warn("Could not record login attempt", ex); }
    }
}
