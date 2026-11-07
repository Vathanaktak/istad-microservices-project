package kh.edu.istad.platform.iam.application.usecase;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class GetUserAuthoritiesUseCase {
    private final UserAssembler assembler;

    public UserAuthoritiesResult execute(UUID userUuid) {
        UserAssembler.Resolved r = assembler.resolve(assembler.require(userUuid));
        return new UserAuthoritiesResult(
            r.roles().stream().map(UserMapper::toRoleAssignment).toList(),
            r.directPermissions().stream().map(UserMapper::toGrant).toList(),
            r.authorities().permissions());
    }
}
