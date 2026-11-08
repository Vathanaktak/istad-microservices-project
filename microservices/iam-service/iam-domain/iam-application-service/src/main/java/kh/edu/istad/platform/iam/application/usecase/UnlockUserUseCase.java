package kh.edu.istad.platform.iam.application.usecase;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Transactional
public class UnlockUserUseCase {
    private final UserAssembler assembler;
    private final UserRepository users;
    private final IamDomainService iamDomainService;

    public void execute(UUID userUuid) {
        User u = assembler.require(userUuid);

        // Domain service validates (not already unlocked) and unlocks the account
        iamDomainService.unlockUser(u);

        users.save(u);
    }
}
