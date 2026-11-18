package kh.edu.istad.platform.iam.application.port.out;

import kh.edu.istad.platform.iam.application.result.PageResult;
import kh.edu.istad.platform.iam.domain.model.LoginRecord;

public interface LoginHistoryRepository {
    LoginRecord save(LoginRecord record);
    PageResult<LoginRecord> findByUsername(String username, String status, int page, int size, String sort);
    PageResult<LoginRecord> findAll(String query, String status, Boolean suspicious, int page, int size, String sort);
}
