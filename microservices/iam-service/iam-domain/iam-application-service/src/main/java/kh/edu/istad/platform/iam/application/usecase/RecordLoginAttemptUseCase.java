package kh.edu.istad.platform.iam.application.usecase;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Transactional
public class RecordLoginAttemptUseCase {
    public static final int MAX_FAILED_ATTEMPTS = 5;
    private final UserRepository users;

    public void execute(String usernameOrEmail, boolean success) {
        users.findByUsernameOrEmail(usernameOrEmail.trim()).ifPresent(u -> {
            if (success) u.recordLoginSuccess(); else u.recordLoginFailure(MAX_FAILED_ATTEMPTS);
            users.save(u);
        });
    }
}
