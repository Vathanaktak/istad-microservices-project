package kh.edu.istad.platform.iam.domain.model;

import java.time.Instant;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Identity extends Auditable {
    private Long userId;
    private IdentityProvider provider;
    private String providerSubject;
    private String email;
    private boolean emailVerified;
    private String displayName;
    private String username;
    private String pictureUrl;
    private Map<String, Object> attributes;
    private Instant linkedAt = Instant.now();
    private Instant lastLoginAt;

    public void recordLogin() {
        this.lastLoginAt = Instant.now();
    }
}
