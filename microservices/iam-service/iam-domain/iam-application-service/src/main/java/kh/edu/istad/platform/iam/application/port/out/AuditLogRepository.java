package kh.edu.istad.platform.iam.application.port.out;

import java.time.Instant;
import kh.edu.istad.platform.iam.application.result.PageResult;
import kh.edu.istad.platform.iam.domain.model.AuditLog;

public interface AuditLogRepository {
    AuditLog save(AuditLog log);
    PageResult<AuditLog> findByUser(Long userId, int page, int size, String sort);
    PageResult<AuditLog> search(String query, String category, String outcome, Instant from, Instant to, int page, int size, String sort);
}
