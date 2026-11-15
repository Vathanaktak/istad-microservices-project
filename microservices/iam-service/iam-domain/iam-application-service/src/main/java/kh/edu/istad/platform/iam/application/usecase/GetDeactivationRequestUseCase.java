package kh.edu.istad.platform.iam.application.usecase;

import java.util.Optional;
import java.util.UUID;
import kh.edu.istad.platform.iam.application.port.out.DeactivationRequestRepository;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.model.DeactivationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetDeactivationRequestUseCase {
    private final DeactivationRequestRepository repository;
    private final UserRepository users;

    public Optional<DeactivationRequest> execute(UUID userUuid) {
        return users.findByUuid(userUuid)
            .flatMap(u -> repository.findPendingByUserId(u.getId()));
    }
}
