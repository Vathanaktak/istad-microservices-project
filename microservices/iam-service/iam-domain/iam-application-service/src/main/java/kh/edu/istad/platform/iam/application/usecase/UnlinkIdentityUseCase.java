package kh.edu.istad.platform.iam.application.usecase;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Transactional
public class UnlinkIdentityUseCase {
    private final UserAssembler assembler;
    private final UserRepository users;
    private final CurrentActorProvider actor;

    public void execute(UUID userUuid, UUID identityUuid) {
        User u = assembler.require(userUuid);
        List<Identity> mine = users.findIdentities(u.getId());
        Identity target = mine.stream().filter(i -> i.getUuid().equals(identityUuid)).findFirst()
            .orElseThrow(() -> NotFoundException.of("Identity", identityUuid));
        int methods = mine.size() + (u.hasPassword() ? 1 : 0);
        if (methods <= 1) throw new BusinessRuleException("last-sign-in-method", "Cannot unlink the last sign-in method");
        target.softDelete(actor.currentActor());
        users.saveIdentity(target);
    }
}
