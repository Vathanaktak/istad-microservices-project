package kh.edu.istad.platform.iam.application.usecase;

import java.time.Instant;
import kh.edu.istad.platform.iam.application.port.out.AuditLogRepository;
import kh.edu.istad.platform.iam.application.result.PageResult;
import kh.edu.istad.platform.iam.domain.model.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAuditLogsUseCase {
    private final AuditLogRepository auditLogs;

    public PageResult<AuditLog> execute(String query, String category, String outcome, Instant from, Instant to, int page, int size, String sort) {
        return auditLogs.search(query, category, outcome, from, to, page, size, sort);
    }
}
