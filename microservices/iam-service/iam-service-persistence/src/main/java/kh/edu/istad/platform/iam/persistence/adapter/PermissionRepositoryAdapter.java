package kh.edu.istad.platform.iam.persistence.adapter;
import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.result.PageResult;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.persistence.entity.*; import kh.edu.istad.platform.iam.persistence.mapper.*; import kh.edu.istad.platform.iam.persistence.repository.*;
import jakarta.persistence.EntityManager; import jakarta.persistence.criteria.Predicate; import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification; import org.springframework.stereotype.Repository; import java.util.*; import static kh.edu.istad.platform.iam.persistence.adapter.PersistenceSupport.*;
@Repository @RequiredArgsConstructor
public class PermissionRepositoryAdapter implements PermissionRepository {
    private static final Set<String> SORTABLE = Set.of("createdAt", "updatedAt", "name", "resource", "action");
    private final PermissionJpaRepository jpa;
    private final PermissionEntityMapper mapper;

    @Override public Optional<Permission> findByUuid(UUID uuid) { return jpa.findByUuid(uuid).map(mapper::toDomain); }
    @Override public List<Permission> findByUuids(Collection<UUID> uuids) { return jpa.findByUuidIn(uuids).stream().map(mapper::toDomain).toList(); }
    @Override public boolean existsByName(String name) { return jpa.existsByName(name); }

    @Override
    public Permission save(Permission p) {
        PermissionEntity e;
        if (p.getId() == null) e = mapper.toNew(p);
        else {
            e = jpa.findById(p.getId()).orElseThrow();
            checkVersion(PermissionEntity.class, p.getId(), p.getVersion(), e.getVersion());
            mapper.update(p, e);
        }
        return mapper.toDomain(jpa.saveAndFlush(e));
    }

    @Override
    public PageResult<Permission> search(PermissionFilterQuery q) {
        Specification<PermissionEntity> spec = (root, cq, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (q.search() != null && !q.search().isBlank())
                ps.add(cb.like(cb.lower(root.get("name")), "%" + q.search().toLowerCase(Locale.ROOT) + "%"));
            if (q.resource() != null && !q.resource().isBlank()) ps.add(cb.equal(root.get("resource"), q.resource().toLowerCase(Locale.ROOT)));
            if (q.active() != null) ps.add(cb.equal(root.get("active"), q.active()));
            return cb.and(ps.toArray(new Predicate[0]));
        };
        return toPage(jpa.findAll(spec, pageable(q.page(), q.size(), q.sort(), SORTABLE)).map(mapper::toDomain));
    }
}
