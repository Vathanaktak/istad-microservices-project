package kh.edu.istad.platform.iam.application.usecase;

import java.util.Optional;
import java.util.UUID;
import kh.edu.istad.platform.iam.application.port.out.DeactivationRequestRepository;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.application.result.DeactivationProposalResult;
import kh.edu.istad.platform.iam.domain.exception.NotFoundException;
import kh.edu.istad.platform.iam.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetDeactivationRequestForUserUseCase {
    private final DeactivationRequestRepository repository;
    private final UserRepository users;

    public Optional<DeactivationProposalResult> execute(UUID userUuid) {
        User u = users.findByUuid(userUuid).orElseThrow(() -> NotFoundException.of("User", userUuid));
        return repository.findPendingByUserId(u.getId()).map(req -> new DeactivationProposalResult(
            req.getUuid(),
            u.getUuid(),
            u.getUsername(),
            u.getEmail(),
            u.getName() != null ? u.getName().fullName() : u.getUsername(),
            req.getReason(),
            req.getDetails(),
            req.getStatus(),
            req.getRequestedAt(),
            req.getCancelledAt(),
            req.getProcessedAt(),
            req.getReviewNotes(),
            req.getReviewedBy()
        ));
    }
}
