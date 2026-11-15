package kh.edu.istad.platform.iam.application.usecase;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import kh.edu.istad.platform.iam.application.port.out.LoginHistoryRepository;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.model.LoginRecord;
import kh.edu.istad.platform.iam.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RecordLoginAttemptUseCase {
    public static final int MAX_FAILED_ATTEMPTS = 5;
    private final UserRepository users;
    private final LoginHistoryRepository loginHistory;

    public void execute(String usernameOrEmail, boolean success) {
        execute(usernameOrEmail, success, null, null, null);
    }

    public void execute(String usernameOrEmail, boolean success, String failureReason, String ipAddress, String userAgent) {
        String cleanName = usernameOrEmail != null ? usernameOrEmail.trim() : "anonymous";
        Optional<User> userOpt = users.findByUsernameOrEmail(cleanName);

        Long userId = null;
        String resolvedUsername = cleanName;
        boolean suspicious = false;

        if (userOpt.isPresent()) {
            User u = userOpt.get();
            userId = u.getId();
            resolvedUsername = u.getUsername();
            if (success) {
                u.recordLoginSuccess();
            } else {
                u.recordLoginFailure(MAX_FAILED_ATTEMPTS);
            }
            users.save(u);
            suspicious = !success && u.getFailedLoginAttempts() >= 3;
        }

        if (loginHistory != null) {
            LoginRecord rec = new LoginRecord();
            rec.setUuid(UUID.randomUUID());
            rec.setUserId(userId);
            rec.setUsername(resolvedUsername);
            rec.setStatus(success ? "SUCCESS" : "FAILED");
            rec.setFailureReason(success ? null : (failureReason != null ? failureReason : "bad_credentials"));
            rec.setIpAddress(ipAddress != null && !ipAddress.isBlank() ? ipAddress : "127.0.0.1");
            rec.setUserAgent(userAgent);
            rec.setDevice(parseDevice(userAgent));
            rec.setBrowser(parseBrowser(userAgent));
            rec.setOs(parseOs(userAgent));
            rec.setLocation("Phnom Penh, KH");
            rec.setSuspicious(suspicious);
            rec.setOccurredAt(Instant.now());
            loginHistory.save(rec);
        }
    }

    private String parseBrowser(String ua) {
        if (ua == null) return "Browser";
        if (ua.contains("Edg")) return "Edge";
        if (ua.contains("Chrome")) return "Chrome";
        if (ua.contains("Safari")) return "Safari";
        if (ua.contains("Firefox")) return "Firefox";
        return "Browser";
    }

    private String parseOs(String ua) {
        if (ua == null) return "Desktop";
        if (ua.contains("Windows")) return "Windows";
        if (ua.contains("Macintosh") || ua.contains("Mac OS")) return "macOS";
        if (ua.contains("Linux")) return "Linux";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("iPhone") || ua.contains("iPad")) return "iOS";
        return "Desktop";
    }

    private String parseDevice(String ua) {
        if (ua == null) return "Desktop";
        if (ua.contains("Mobi") || ua.contains("Android") || ua.contains("iPhone")) return "Mobile";
        if (ua.contains("Tablet") || ua.contains("iPad")) return "Tablet";
        return "Desktop";
    }
}
