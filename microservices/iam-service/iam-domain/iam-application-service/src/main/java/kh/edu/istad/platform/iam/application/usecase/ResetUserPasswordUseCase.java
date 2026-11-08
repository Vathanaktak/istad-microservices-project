package kh.edu.istad.platform.iam.application.usecase;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Transactional
public class ResetUserPasswordUseCase {
    private final UserAssembler assembler;
    private final UserRepository users;
    private final PasswordHasher hasher;
    private final IamDomainService iamDomainService;

    public void execute(ResetUserPasswordCommand c) {
        User u = assembler.require(c.userUuid());

        // Domain service validates and changes the password
        iamDomainService.changeUserPassword(u, hasher.hash(c.newPassword()));

        // If the account was locked, also unlock it
        if (!u.isAccountNonLocked()) {
            iamDomainService.unlockUser(u);
        }

        users.save(u);
    }
}
