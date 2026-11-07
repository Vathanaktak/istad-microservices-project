package kh.edu.istad.platform.iam.domain.model;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RolePermission extends Assignment {
    private Long roleId;
    private Permission permission;

    public static RolePermission create(Long roleId, Permission p, String by, Instant expiresAt, String reason) {
        RolePermission a = new RolePermission();
        a.roleId = roleId; a.permission = p; a.init(by, expiresAt, reason);
        return a;
    }
}
