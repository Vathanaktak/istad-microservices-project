package kh.edu.istad.platform.iam.application.usecase;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class LoadUserForAuthenticationUseCase {
    private final UserRepository users;
    private final UserAssembler assembler;

    public AuthUserResult execute(String usernameOrEmail) {
        User u = users.findByUsernameOrEmail(usernameOrEmail.trim())
            .orElseThrow(() -> NotFoundException.of("User", usernameOrEmail));
        return assembler.authUser(u);
    }
}
