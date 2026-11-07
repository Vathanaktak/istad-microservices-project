package kh.edu.istad.platform.iam.persistence.adapter;

import kh.edu.istad.platform.iam.application.result.PageResult;
import java.util.Set;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.*;

final class PersistenceSupport {
    private PersistenceSupport() {}

    static void checkVersion(Class<?> type, Object id, Long domainVersion, Long entityVersion) {
        if (domainVersion != null && !domainVersion.equals(entityVersion))
            throw new org.springframework.orm.ObjectOptimisticLockingFailureException(type, id);
    }

    /** sort = "field,asc|desc"; unknown fields fall back to createdAt desc. */
    static Pageable pageable(int page, int size, String sort, Set<String> allowed) {
        int s = Math.min(Math.max(size, 1), 100);
        Sort order = Sort.by(Sort.Direction.DESC, "createdAt");
        if (sort != null && !sort.isBlank()) {
            String[] p = sort.split(",");
            if (allowed.contains(p[0]))
                order = Sort.by(p.length > 1 && p[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, p[0]);
        }
        return PageRequest.of(Math.max(page, 0), s, order);
    }

    static <T> PageResult<T> toPage(Page<T> p) {
        return new PageResult<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
    }
}
