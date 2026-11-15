package kh.edu.istad.platform.iam.infrastructure.adapter;

import kh.edu.istad.platform.iam.application.port.out.PasswordResetNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogPasswordResetNotifierAdapter implements PasswordResetNotifier {
    private static final Logger log = LoggerFactory.getLogger(LogPasswordResetNotifierAdapter.class);

    @Override
    public void notifyPasswordReset(String email, String plainToken) {
        log.info("Password reset requested for {}. Token: {} (Use at /reset-password?token={})", email, plainToken, plainToken);
    }
}
