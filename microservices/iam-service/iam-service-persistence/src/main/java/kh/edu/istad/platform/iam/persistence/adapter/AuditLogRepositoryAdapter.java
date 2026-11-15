package kh.edu.istad.platform.iam.persistence.adapter;

import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.*;
import kh.edu.istad.platform.iam.application.port.out.AuditLogRepository;
import kh.edu.istad.platform.iam.application.result.PageResult;
import kh.edu.istad.platform.iam.domain.model.AuditLog;
import kh.edu.istad.platform.iam.persistence.entity.AuditLogEntity;
import kh.edu.istad.platform.iam.persistence.repository.AuditLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuditLogRepositoryAdapter implements AuditLogRepository {
    private final AuditLogJpaRepository jpa;

    @Override
    public AuditLog save(AuditLog log) {
        AuditLogEntity e = new AuditLogEntity();
        e.setUuid(log.getUuid() != null ? log.getUuid() : UUID.randomUUID());
        e.setUserId(log.getUserId());
        e.setCategory(log.getCategory());
        e.setAction(log.getAction());
        e.setOutcome(log.getOutcome());
        e.setActor(log.getActor());
        e.setTarget(log.getTarget());
        e.setIpAddress(log.getIpAddress());
        e.setDetail(log.getDetail());
        e.setOccurredAt(log.getOccurredAt() != null ? log.getOccurredAt() : Instant.now());
        AuditLogEntity saved = jpa.saveAndFlush(e);
        log.setId(saved.getId());
        log.setUuid(saved.getUuid());
        return log;
    }

    @Override
    public PageResult<AuditLog> findByUser(Long userId, int page, int size, String sort) {
        Pageable pageable = pageable(page, size, sort);
        return toPageResult(jpa.findByUserId(userId, pageable));
    }

    @Override
    public PageResult<AuditLog> search(String query, String category, String outcome, Instant from, Instant to, int page, int size, String sort) {
        Specification<AuditLogEntity> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query != null && !query.isBlank()) {
                String like = "%" + query.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("actor")), like),
                    cb.like(cb.lower(root.get("target")), like),
                    cb.like(cb.lower(root.get("action")), like),
                    cb.like(cb.lower(root.get("detail")), like)
                ));
            }
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (outcome != null && !outcome.isBlank()) {
                predicates.add(cb.equal(root.get("outcome"), outcome));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("occurredAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("occurredAt"), to));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return toPageResult(jpa.findAll(spec, pageable(page, size, sort)));
    }

    private Pageable pageable(int page, int size, String sort) {
        Sort s = Sort.by(Sort.Direction.DESC, "occurredAt");
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            s = Sort.by("desc".equalsIgnoreCase(parts.length > 1 ? parts[1] : "asc") ? Sort.Direction.DESC : Sort.Direction.ASC, parts[0]);
        }
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), s);
    }

    private PageResult<AuditLog> toPageResult(Page<AuditLogEntity> page) {
        List<AuditLog> items = page.getContent().stream().map(this::toDomain).toList();
        return new PageResult<>(items, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private AuditLog toDomain(AuditLogEntity e) {
        AuditLog d = new AuditLog();
        d.setId(e.getId());
        d.setUuid(e.getUuid());
        d.setUserId(e.getUserId());
        d.setCategory(e.getCategory());
        d.setAction(e.getAction());
        d.setOutcome(e.getOutcome());
        d.setActor(e.getActor());
        d.setTarget(e.getTarget());
        d.setIpAddress(e.getIpAddress());
        d.setDetail(e.getDetail());
        d.setOccurredAt(e.getOccurredAt());
        return d;
    }
}
