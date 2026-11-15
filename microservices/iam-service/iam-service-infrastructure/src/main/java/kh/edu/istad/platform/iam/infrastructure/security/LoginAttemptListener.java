package kh.edu.istad.platform.iam.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import kh.edu.istad.platform.iam.application.usecase.RecordLoginAttemptUseCase;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
public class LoginAttemptListener {
    private static final Logger log = LoggerFactory.getLogger(LoginAttemptListener.class);
    private final RecordLoginAttemptUseCase recordAttempt;

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent e) {
        if (e.getAuthentication() instanceof UsernamePasswordAuthenticationToken) {
            record(e.getAuthentication().getName(), true, null);
        }
    }

    @EventListener
    public void onBadCredentials(AuthenticationFailureBadCredentialsEvent e) {
        if (e.getAuthentication() instanceof UsernamePasswordAuthenticationToken) {
            record(e.getAuthentication().getName(), false, "bad_credentials");
        }
    }

    @EventListener
    public void onLocked(AuthenticationFailureLockedEvent e) {
        if (e.getAuthentication() instanceof UsernamePasswordAuthenticationToken) {
            record(e.getAuthentication().getName(), false, "locked");
        }
    }

    @EventListener
    public void onDisabled(AuthenticationFailureDisabledEvent e) {
        if (e.getAuthentication() instanceof UsernamePasswordAuthenticationToken) {
            record(e.getAuthentication().getName(), false, "disabled");
        }
    }

    @EventListener
    public void onExpired(AuthenticationFailureExpiredEvent e) {
        if (e.getAuthentication() instanceof UsernamePasswordAuthenticationToken) {
            record(e.getAuthentication().getName(), false, "expired");
        }
    }

    @EventListener
    public void onCredentialsExpired(AuthenticationFailureCredentialsExpiredEvent e) {
        if (e.getAuthentication() instanceof UsernamePasswordAuthenticationToken) {
            record(e.getAuthentication().getName(), false, "credentials_expired");
        }
    }

    private void record(String name, boolean ok, String failureReason) {
        try {
            if (name != null && !name.isBlank()) {
                String ip = "127.0.0.1";
                String userAgent = null;

                try {
                    ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attrs != null) {
                        HttpServletRequest req = attrs.getRequest();
                        ip = extractIp(req);
                        userAgent = req.getHeader("User-Agent");
                    }
                } catch (Exception ignored) {
                }

                recordAttempt.execute(name, ok, failureReason, ip, userAgent);
            }
        } catch (RuntimeException ex) {
            log.warn("Could not record login attempt", ex);
        }
    }

    private String extractIp(HttpServletRequest req) {
        String xf = req.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return req.getRemoteAddr() != null ? req.getRemoteAddr() : "127.0.0.1";
    }
}
