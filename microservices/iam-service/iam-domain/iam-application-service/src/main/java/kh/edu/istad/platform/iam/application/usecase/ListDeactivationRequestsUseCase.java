package kh.edu.istad.platform.iam.application.usecase;

import java.util.UUID;
import kh.edu.istad.platform.iam.application.port.out.DeactivationRequestRepository;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.application.result.DeactivationProposalResult;
import kh.edu.istad.platform.iam.application.result.PageResult;
import kh.edu.istad.platform.iam.domain.model.DeactivationRequest;
import kh.edu.istad.platform.iam.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListDeactivationRequestsUseCase {
    private final DeactivationRequestRepository repository;
    private final UserRepository users;

    public PageResult<DeactivationProposalResult> execute(String status, int page, int size) {
        PageResult<DeactivationRequest> result = repository.findAll(status, page, size);
        return result.map(req -> {
            User u = users.findById(req.getUserId()).orElse(null);
            String username = u != null ? u.getUsername() : "unknown";
            String email = u != null ? u.getEmail() : "";
            String name = u != null && u.getName() != null ? u.getName().fullName() : username;
            UUID userUuid = u != null ? u.getUuid() : null;

            return new DeactivationProposalResult(
                req.getUuid(),
                userUuid,
                username,
                email,
                name,
                req.getReason(),
                req.getDetails(),
                req.getStatus(),
                req.getRequestedAt(),
                req.getCancelledAt(),
                req.getProcessedAt(),
                req.getReviewNotes(),
                req.getReviewedBy()
            );
        });
    }

    public long countPending() {
        return repository.countByStatus("PENDING");
    }
}
