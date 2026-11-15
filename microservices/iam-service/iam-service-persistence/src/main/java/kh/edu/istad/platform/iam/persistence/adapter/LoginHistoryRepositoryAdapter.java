package kh.edu.istad.platform.iam.persistence.adapter;

import jakarta.persistence.criteria.Predicate;
import java.util.*;
import kh.edu.istad.platform.iam.application.port.out.LoginHistoryRepository;
import kh.edu.istad.platform.iam.application.result.PageResult;
import kh.edu.istad.platform.iam.domain.model.LoginRecord;
import kh.edu.istad.platform.iam.persistence.entity.LoginHistoryEntity;
import kh.edu.istad.platform.iam.persistence.repository.LoginHistoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LoginHistoryRepositoryAdapter implements LoginHistoryRepository {
    private final LoginHistoryJpaRepository jpa;

    @Override
    public LoginRecord save(LoginRecord r) {
        LoginHistoryEntity e = new LoginHistoryEntity();
        e.setUuid(r.getUuid() != null ? r.getUuid() : UUID.randomUUID());
        e.setUserId(r.getUserId());
        e.setUsername(r.getUsername());
        e.setStatus(r.getStatus());
        e.setIpAddress(r.getIpAddress());
        e.setLocation(r.getLocation());
        e.setUserAgent(r.getUserAgent());
        e.setDevice(r.getDevice());
        e.setBrowser(r.getBrowser());
        e.setOs(r.getOs());
        e.setSuspicious(r.isSuspicious());
        e.setFailureReason(r.getFailureReason());
        e.setOccurredAt(r.getOccurredAt() != null ? r.getOccurredAt() : java.time.Instant.now());
        LoginHistoryEntity saved = jpa.saveAndFlush(e);
        r.setId(saved.getId());
        r.setUuid(saved.getUuid());
        return r;
    }

    @Override
    public PageResult<LoginRecord> findByUsername(String username, String status, int page, int size, String sort) {
        Pageable pageable = pageable(page, size, sort);
        Page<LoginHistoryEntity> res = (status != null && !status.isBlank())
            ? jpa.findByUsernameIgnoreCaseAndStatus(username, status, pageable)
            : jpa.findByUsernameIgnoreCase(username, pageable);
        return toPageResult(res);
    }

    @Override
    public PageResult<LoginRecord> findAll(String query, String status, Boolean suspicious, int page, int size, String sort) {
        Specification<LoginHistoryEntity> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query != null && !query.isBlank()) {
                String like = "%" + query.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("username")), like),
                    cb.like(cb.lower(root.get("ipAddress")), like),
                    cb.like(cb.lower(root.get("location")), like)
                ));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (suspicious != null) {
                predicates.add(cb.equal(root.get("suspicious"), suspicious));
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

    private PageResult<LoginRecord> toPageResult(Page<LoginHistoryEntity> page) {
        List<LoginRecord> items = page.getContent().stream().map(this::toDomain).toList();
        return new PageResult<>(items, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private LoginRecord toDomain(LoginHistoryEntity e) {
        LoginRecord r = new LoginRecord();
        r.setId(e.getId());
        r.setUuid(e.getUuid());
        r.setUserId(e.getUserId());
        r.setUsername(e.getUsername());
        r.setStatus(e.getStatus());
        r.setIpAddress(e.getIpAddress());
        r.setLocation(e.getLocation());
        r.setUserAgent(e.getUserAgent());
        r.setDevice(e.getDevice());
        r.setBrowser(e.getBrowser());
        r.setOs(e.getOs());
        r.setSuspicious(e.isSuspicious());
        r.setFailureReason(e.getFailureReason());
        r.setOccurredAt(e.getOccurredAt());
        return r;
    }
}
