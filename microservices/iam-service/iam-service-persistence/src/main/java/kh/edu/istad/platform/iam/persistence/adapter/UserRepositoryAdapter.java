package kh.edu.istad.platform.iam.persistence.adapter;
import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.result.PageResult;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.persistence.entity.*; import kh.edu.istad.platform.iam.persistence.mapper.*; import kh.edu.istad.platform.iam.persistence.repository.*;
import jakarta.persistence.EntityManager; import jakarta.persistence.criteria.Predicate; import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification; import org.springframework.stereotype.Repository; import java.util.*; import static kh.edu.istad.platform.iam.persistence.adapter.PersistenceSupport.*;
@Repository @RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {
    private static final Set<String> SORTABLE = Set.of("createdAt", "updatedAt", "username", "email", "givenName", "familyName");
    private final UserJpaRepository jpa;
    private final UserEntityMapper mapper;
    private final UserRoleJpaRepository userRoles;
    private final UserPermissionJpaRepository userPermissions;
    private final IdentityJpaRepository identities;
    private final IdentityEntityMapper identityMapper;
    private final RoleEntityMapper roleMapper;
    private final PermissionEntityMapper permissionMapper;
    private final EntityManager em;

    @Override public Optional<User> findByUuid(UUID uuid) { return jpa.findByUuid(uuid).map(mapper::toDomain); }
    @Override public Optional<User> findById(Long id) { return jpa.findById(id).map(mapper::toDomain); }
    @Override public Optional<User> findByUsernameOrEmail(String v) { return jpa.findByUsernameOrEmail(v).map(mapper::toDomain); }
    @Override public boolean existsByUsername(String v) { return jpa.existsByUsernameIgnoreCase(v); }
    @Override public boolean existsByEmail(String v) { return jpa.existsByEmail(v.toLowerCase(Locale.ROOT)); }
    @Override public boolean existsByPhoneNumber(String v) { return jpa.existsByPhoneNumber(v); }

    @Override
    public User save(User u) {
        UserEntity e;
        if (u.getId() == null) e = mapper.toNew(u);
        else {
            e = jpa.findById(u.getId()).orElseThrow();
            checkVersion(UserEntity.class, u.getId(), u.getVersion(), e.getVersion());
            mapper.update(u, e);
        }
        return mapper.toDomain(jpa.saveAndFlush(e));
    }

    @Override
    public PageResult<User> search(UserFilterQuery q) {
        Specification<UserEntity> spec = (root, cq, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (q.search() != null && !q.search().isBlank()) {
                String like = "%" + q.search().toLowerCase(Locale.ROOT) + "%";
                ps.add(cb.or(cb.like(cb.lower(root.get("username")), like), cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("givenName")), like), cb.like(cb.lower(root.get("familyName")), like)));
            }
            if (q.enabled() != null) ps.add(cb.equal(root.get("enabled"), q.enabled()));
            return cb.and(ps.toArray(new Predicate[0]));
        };
        return toPage(jpa.findAll(spec, pageable(q.page(), q.size(), q.sort(), SORTABLE)).map(mapper::toDomain));
    }

    // ---- role assignments
    @Override
    public List<UserRole> findUserRoles(Long userId) {
        return userRoles.findAllWithRole(userId).stream().map(this::toDomain).toList();
    }
    @Override
    public UserRole saveUserRole(UserRole a) {
        UserRoleEntity e = a.getId() == null ? new UserRoleEntity() : userRoles.findById(a.getId()).orElseThrow();
        e.setUserId(a.getUserId());
        e.setRole(em.getReference(RoleEntity.class, a.getRole().getId()));
        copy(a, e);
        return toDomain(userRoles.saveAndFlush(e));
    }
    private UserRole toDomain(UserRoleEntity e) {
        UserRole d = new UserRole();
        d.setId(e.getId()); d.setUserId(e.getUserId()); d.setRole(roleMapper.toDomain(e.getRole()));
        copy(e, d);
        return d;
    }

    // ---- direct permissions
    @Override
    public List<UserPermission> findUserPermissions(Long userId) {
        return userPermissions.findAllWithPermission(userId).stream().map(this::toDomain).toList();
    }
    @Override
    public UserPermission saveUserPermission(UserPermission a) {
        UserPermissionEntity e = a.getId() == null ? new UserPermissionEntity() : userPermissions.findById(a.getId()).orElseThrow();
        e.setUserId(a.getUserId());
        e.setPermission(em.getReference(PermissionEntity.class, a.getPermission().getId()));
        e.setEffect(a.getEffect());
        copy(a, e);
        return toDomain(userPermissions.saveAndFlush(e));
    }
    private UserPermission toDomain(UserPermissionEntity e) {
        UserPermission d = new UserPermission();
        d.setId(e.getId()); d.setUserId(e.getUserId()); d.setEffect(e.getEffect());
        d.setPermission(permissionMapper.toDomain(e.getPermission()));
        copy(e, d);
        return d;
    }

    // ---- identities
    @Override public Optional<Identity> findIdentity(IdentityProvider p, String subject) {
        return identities.findByProviderAndProviderSubject(p, subject).map(identityMapper::toDomain);
    }
    @Override public List<Identity> findIdentities(Long userId) {
        return identities.findByUserIdOrderByLinkedAtAsc(userId).stream().map(identityMapper::toDomain).toList();
    }
    @Override
    public Identity saveIdentity(Identity i) {
        IdentityEntity e;
        if (i.getId() == null) e = identityMapper.toNew(i);
        else {
            e = identities.findById(i.getId()).orElseThrow();
            checkVersion(IdentityEntity.class, i.getId(), i.getVersion(), e.getVersion());
            identityMapper.update(i, e);
        }
        return identityMapper.toDomain(identities.saveAndFlush(e));
    }

    static void copy(Assignment s, AssignmentEntity t) {
        t.setAssignedAt(s.getAssignedAt()); t.setAssignedBy(s.getAssignedBy()); t.setExpiresAt(s.getExpiresAt());
        t.setRevokedAt(s.getRevokedAt()); t.setRevokedBy(s.getRevokedBy()); t.setReason(s.getReason());
    }
    static void copy(AssignmentEntity s, Assignment t) {
        t.setAssignedAt(s.getAssignedAt()); t.setAssignedBy(s.getAssignedBy()); t.setExpiresAt(s.getExpiresAt());
        t.setRevokedAt(s.getRevokedAt()); t.setRevokedBy(s.getRevokedBy()); t.setReason(s.getReason());
    }
}
