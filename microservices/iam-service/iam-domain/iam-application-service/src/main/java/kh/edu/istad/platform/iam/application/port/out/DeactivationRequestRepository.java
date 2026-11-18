package kh.edu.istad.platform.iam.application.port.out;

import java.util.Optional;
import java.util.UUID;
import kh.edu.istad.platform.iam.application.result.PageResult;
import kh.edu.istad.platform.iam.domain.model.DeactivationRequest;

public interface DeactivationRequestRepository {
    DeactivationRequest save(DeactivationRequest request);
    Optional<DeactivationRequest> findPendingByUserId(Long userId);
    Optional<DeactivationRequest> findByUuid(UUID uuid);
    PageResult<DeactivationRequest> findAll(String status, int page, int size);
    long countByStatus(String status);
    boolean hasPendingRequest(Long userId);
}
