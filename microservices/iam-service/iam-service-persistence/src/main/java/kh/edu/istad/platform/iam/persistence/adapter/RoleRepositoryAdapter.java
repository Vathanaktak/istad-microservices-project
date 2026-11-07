package kh.edu.istad.platform.iam.persistence.adapter;
import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.result.PageResult;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.persistence.entity.*; import kh.edu.istad.platform.iam.persistence.mapper.*; import kh.edu.istad.platform.iam.persistence.repository.*;
import jakarta.persistence.EntityManager; import jakarta.persistence.criteria.Predicate; import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification; import org.springframework.stereotype.Repository; import java.util.*; import static kh.edu.istad.platform.iam.persistence.adapter.PersistenceSupport.*;
@Repository @RequiredArgsConstructor
public class RoleRepositoryAdapter implements RoleRepository {
    private static final Set<String> SORTABLE = Set.of("createdAt", "updatedAt", "name");
    private final RoleJpaRepository jpa;
    private final RoleEntityMapper mapper;
    private final RolePermissionJpaRepository rolePermissions;
    private final PermissionEntityMapper permissionMapper;
    private final EntityManager em;

    @Override public Optional<Role> findByUuid(UUID uuid) { return jpa.findByUuid(uuid).map(mapper::toDomain); }
    @Override public List<Role> findByUuids(Collection<UUID> uuids) { return jpa.findByUuidIn(uuids).stream().map(mapper::toDomain).toList(); }
    @Override public Optional<Role> findByName(String name) { return jpa.findByName(name).map(mapper::toDomain); }
    @Override public boolean existsByName(String name) { return jpa.existsByName(name); }

    @Override
    public Role save(Role r) {
        RoleEntity e;
        if (r.getId() == null) e = mapper.toNew(r);
        else {
            e = jpa.findById(r.getId()).orElseThrow();
            checkVersion(RoleEntity.class, r.getId(), r.getVersion(), e.getVersion());
            mapper.update(r, e);
        }
        return mapper.toDomain(jpa.saveAndFlush(e));
    }

    @Override
    public PageResult<Role> search(RoleFilterQuery q) {
        Specification<RoleEntity> spec = (root, cq, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (q.search() != null && !q.search().isBlank())
                ps.add(cb.like(cb.lower(root.get("name")), "%" + q.search().toLowerCase(Locale.ROOT) + "%"));
            if (q.active() != null) ps.add(cb.equal(root.get("active"), q.active()));
            return cb.and(ps.toArray(new Predicate[0]));
        };
        return toPage(jpa.findAll(spec, pageable(q.page(), q.size(), q.sort(), SORTABLE)).map(mapper::toDomain));
    }

    @Override public List<RolePermission> findRolePermissions(Long roleId) { return findRolePermissionsByRoleIds(List.of(roleId)); }
    @Override
    public List<RolePermission> findRolePermissionsByRoleIds(Collection<Long> roleIds) {
        if (roleIds.isEmpty()) return List.of();
        return rolePermissions.findAllWithPermission(roleIds).stream().map(this::toDomain).toList();
    }
    @Override
    public RolePermission saveRolePermission(RolePermission a) {
        RolePermissionEntity e = a.getId() == null ? new RolePermissionEntity() : rolePermissions.findById(a.getId()).orElseThrow();
        e.setRoleId(a.getRoleId());
        e.setPermission(em.getReference(PermissionEntity.class, a.getPermission().getId()));
        UserRepositoryAdapter.copy(a, e);
        return toDomain(rolePermissions.saveAndFlush(e));
    }
    private RolePermission toDomain(RolePermissionEntity e) {
        RolePermission d = new RolePermission();
        d.setId(e.getId()); d.setRoleId(e.getRoleId()); d.setPermission(permissionMapper.toDomain(e.getPermission()));
        UserRepositoryAdapter.copy(e, d);
        return d;
    }
}
