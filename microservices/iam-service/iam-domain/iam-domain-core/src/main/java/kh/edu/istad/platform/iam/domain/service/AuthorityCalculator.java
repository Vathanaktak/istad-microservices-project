package kh.edu.istad.platform.iam.domain.service;

import kh.edu.istad.platform.iam.domain.model.*;
import java.time.Instant;
import java.util.*;

/** Effective permissions = active role permissions + direct GRANT - direct DENY. */
public final class AuthorityCalculator {
    private AuthorityCalculator() {}

    public record Result(Set<String> roles, Set<String> permissions) {}

    public static Result calculate(Collection<UserRole> userRoles, Collection<RolePermission> rolePermissions,
                                   Collection<UserPermission> userPermissions, Instant now) {
        Set<String> roles = new TreeSet<>();
        Set<Long> roleIds = new HashSet<>();
        for (UserRole ur : userRoles) {
            Role r = ur.getRole();
            if (ur.isActive(now) && r != null && r.isActive() && !r.isDeleted()) {
                roles.add(r.getName());
                roleIds.add(r.getId());
            }
        }
        Set<String> perms = new TreeSet<>();
        for (RolePermission rp : rolePermissions) {
            if (roleIds.contains(rp.getRoleId()) && rp.isActive(now) && usable(rp.getPermission()))
                perms.add(rp.getPermission().getName());
        }
        Set<String> denied = new HashSet<>();
        for (UserPermission up : userPermissions) {
            if (!up.isActive(now) || !usable(up.getPermission())) continue;
            if (up.getEffect() == Effect.GRANT) perms.add(up.getPermission().getName());
            else denied.add(up.getPermission().getName());
        }
        perms.removeAll(denied);
        return new Result(roles, perms);
    }

    private static boolean usable(Permission p) { return p != null && p.isActive() && !p.isDeleted(); }
}
