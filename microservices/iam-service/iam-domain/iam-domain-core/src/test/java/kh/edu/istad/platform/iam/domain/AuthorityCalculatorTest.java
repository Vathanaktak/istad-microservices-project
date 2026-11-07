package kh.edu.istad.platform.iam.domain;

import static org.junit.jupiter.api.Assertions.*;

import kh.edu.istad.platform.iam.domain.model.*;
import kh.edu.istad.platform.iam.domain.service.AuthorityCalculator;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class AuthorityCalculatorTest {
    private Role role(long id, String n) { Role r = new Role(); r.setId(id); r.setName(n); return r; }
    private Permission perm(String n) { Permission p = new Permission(); p.setName(n); return p; }

    @Test
    void effective_is_rolePermissions_plus_grant_minus_deny() {
        Instant now = Instant.now();
        UserRole ur = UserRole.create(1L, role(10, "ADMIN"), "t", null, null);
        RolePermission rp1 = RolePermission.create(10L, perm("user:read"), "t", null, null);
        RolePermission rp2 = RolePermission.create(10L, perm("user:update"), "t", null, null);
        UserPermission grant = UserPermission.create(1L, perm("report:read"), Effect.GRANT, "t", null, null);
        UserPermission deny = UserPermission.create(1L, perm("user:update"), Effect.DENY, "t", null, null);

        var r = AuthorityCalculator.calculate(List.of(ur), List.of(rp1, rp2), List.of(grant, deny), now);

        assertEquals(java.util.Set.of("ADMIN"), r.roles());
        assertEquals(java.util.Set.of("user:read", "report:read"), r.permissions());
    }

    @Test
    void revoked_and_expired_assignments_are_ignored() {
        Instant now = Instant.now();
        UserRole revoked = UserRole.create(1L, role(10, "ADMIN"), "t", null, null);
        revoked.revoke("t", "x");
        UserRole expired = UserRole.create(1L, role(11, "USER"), "t", now.minusSeconds(5), null);
        var r = AuthorityCalculator.calculate(List.of(revoked, expired), List.of(), List.of(), now);
        assertTrue(r.roles().isEmpty());
    }
}
