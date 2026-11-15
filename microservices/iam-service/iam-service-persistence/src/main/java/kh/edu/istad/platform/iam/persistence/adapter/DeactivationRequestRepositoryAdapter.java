package kh.edu.istad.platform.iam.persistence.adapter;

import java.util.Optional;
import java.util.UUID;
import kh.edu.istad.platform.iam.application.port.out.DeactivationRequestRepository;
import kh.edu.istad.platform.iam.application.result.PageResult;
import kh.edu.istad.platform.iam.domain.model.DeactivationRequest;
import kh.edu.istad.platform.iam.persistence.entity.DeactivationRequestEntity;
import kh.edu.istad.platform.iam.persistence.repository.DeactivationRequestJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DeactivationRequestRepositoryAdapter implements DeactivationRequestRepository {
    private final DeactivationRequestJpaRepository jpa;

    @Override
    public DeactivationRequest save(DeactivationRequest req) {
        DeactivationRequestEntity e;
        if (req.getId() == null) {
            e = new DeactivationRequestEntity();
            e.setUuid(req.getUuid() != null ? req.getUuid() : UUID.randomUUID());
        } else {
            e = jpa.findById(req.getId()).orElseThrow();
        }
        e.setUserId(req.getUserId());
        e.setReason(req.getReason());
        e.setDetails(req.getDetails());
        e.setStatus(req.getStatus());
        e.setRequestedAt(req.getRequestedAt());
        e.setCancelledAt(req.getCancelledAt());
        e.setProcessedAt(req.getProcessedAt());
        e.setReviewNotes(req.getReviewNotes());
        e.setReviewedBy(req.getReviewedBy());
        DeactivationRequestEntity saved = jpa.saveAndFlush(e);
        req.setId(saved.getId());
        req.setUuid(saved.getUuid());
        return req;
    }

    @Override
    public Optional<DeactivationRequest> findPendingByUserId(Long userId) {
        return jpa.findFirstByUserIdAndStatusOrderByRequestedAtDesc(userId, "PENDING").map(this::toDomain);
    }

    @Override
    public Optional<DeactivationRequest> findByUuid(UUID uuid) {
        return jpa.findByUuid(uuid).map(this::toDomain);
    }

    @Override
    public PageResult<DeactivationRequest> findAll(String status, int page, int size) {
        PageRequest pr = PageRequest.of(page, Math.max(1, size));
        Page<DeactivationRequestEntity> p;
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            p = jpa.findByStatusOrderByRequestedAtDesc(status.toUpperCase(), pr);
        } else {
            p = jpa.findAllByOrderByRequestedAtDesc(pr);
        }
        return new PageResult<>(
            p.getContent().stream().map(this::toDomain).toList(),
            p.getNumber(),
            p.getSize(),
            p.getTotalElements(),
            p.getTotalPages()
        );
    }

    @Override
    public long countByStatus(String status) {
        return jpa.countByStatus(status);
    }

    @Override
    public boolean hasPendingRequest(Long userId) {
        return jpa.existsByUserIdAndStatus(userId, "PENDING");
    }

    private DeactivationRequest toDomain(DeactivationRequestEntity e) {
        DeactivationRequest r = new DeactivationRequest();
        r.setId(e.getId());
        r.setUuid(e.getUuid());
        r.setUserId(e.getUserId());
        r.setReason(e.getReason());
        r.setDetails(e.getDetails());
        r.setStatus(e.getStatus());
        r.setRequestedAt(e.getRequestedAt());
        r.setCancelledAt(e.getCancelledAt());
        r.setProcessedAt(e.getProcessedAt());
        r.setReviewNotes(e.getReviewNotes());
        r.setReviewedBy(e.getReviewedBy());
        return r;
    }
}
