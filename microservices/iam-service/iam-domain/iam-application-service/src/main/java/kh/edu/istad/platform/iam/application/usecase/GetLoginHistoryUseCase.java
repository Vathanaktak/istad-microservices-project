package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.port.out.LoginHistoryRepository;
import kh.edu.istad.platform.iam.application.result.PageResult;
import kh.edu.istad.platform.iam.domain.model.LoginRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetLoginHistoryUseCase {
    private final LoginHistoryRepository repository;

    public PageResult<LoginRecord> executeForUser(String username, String status, int page, int size, String sort) {
        return repository.findByUsername(username, status, page, size, sort);
    }

    public PageResult<LoginRecord> executeGlobal(String query, String status, Boolean suspicious, int page, int size, String sort) {
        return repository.findAll(query, status, suspicious, page, size, sort);
    }
}
