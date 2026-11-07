package kh.edu.istad.platform.iam.application.usecase;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Transactional
public class RegisterUserUseCase {
    private final UserCreator creator;
    private final UserAssembler assembler;

    public UserResult execute(RegisterUserCommand c) {
        User u = creator.create(new NewUser(c.username(), c.email(), false, c.password(), c.givenName(), c.familyName(),
            null, null, null, null, c.locale(), null, null, null));
        return assembler.result(u);
    }
}
