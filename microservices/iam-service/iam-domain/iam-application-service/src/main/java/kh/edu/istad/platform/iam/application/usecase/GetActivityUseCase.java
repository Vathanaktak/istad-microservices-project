package kh.edu.istad.platform.iam.application.usecase;

import java.util.UUID;
import kh.edu.istad.platform.iam.application.port.out.AuditLogRepository;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.application.result.PageResult;
import kh.edu.istad.platform.iam.domain.exception.NotFoundException;
import kh.edu.istad.platform.iam.domain.model.AuditLog;
import kh.edu.istad.platform.iam.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetActivityUseCase {
    private final AuditLogRepository auditLogs;
    private final UserRepository users;

    public PageResult<AuditLog> execute(UUID userUuid, int page, int size, String sort) {
        User u = users.findByUuid(userUuid).orElseThrow(() -> NotFoundException.of("User", userUuid));
        return auditLogs.findByUser(u.getId(), page, size, sort);
    }
}
