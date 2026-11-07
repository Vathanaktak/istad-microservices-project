package kh.edu.istad.platform.iam.domain.model;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserPermission extends Assignment {
    private Long userId;
    private Permission permission;
    private Effect effect = Effect.GRANT;

    public static UserPermission create(Long userId, Permission p, Effect effect, String by, Instant expiresAt, String reason) {
        UserPermission a = new UserPermission();
        a.userId = userId; a.permission = p; a.effect = effect; a.init(by, expiresAt, reason);
        return a;
    }
}
