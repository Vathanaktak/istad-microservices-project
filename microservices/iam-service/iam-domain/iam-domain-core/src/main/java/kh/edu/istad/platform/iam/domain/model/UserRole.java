package kh.edu.istad.platform.iam.domain.model;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserRole extends Assignment {
    private Long userId;
    private Role role;

    public static UserRole create(Long userId, Role role, String by, Instant expiresAt, String reason) {
        UserRole a = new UserRole();
        a.userId = userId; a.role = role; a.init(by, expiresAt, reason);
        return a;
    }
}
