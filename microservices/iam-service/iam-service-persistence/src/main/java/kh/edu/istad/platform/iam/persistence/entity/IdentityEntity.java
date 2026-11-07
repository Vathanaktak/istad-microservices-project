package kh.edu.istad.platform.iam.persistence.entity;
import jakarta.persistence.*; import lombok.*; import java.time.*; import java.util.*; import org.hibernate.annotations.SQLRestriction; import org.hibernate.annotations.JdbcTypeCode;
@Entity @Table(name = "identities") @SQLRestriction("is_deleted = false")
@Getter @Setter
public class IdentityEntity extends BaseEntity {
    @Column(name = "user_id", nullable = false) private Long userId;
    @Enumerated(EnumType.STRING) private kh.edu.istad.platform.iam.domain.model.IdentityProvider provider;
    private String providerSubject;
    private String email;
    private boolean emailVerified;
    private String displayName;
    private String username;
    private String pictureUrl;
    @JdbcTypeCode(org.hibernate.type.SqlTypes.JSON) @Column(columnDefinition = "jsonb")
    private Map<String, Object> attributes;
    private Instant linkedAt;
    private Instant lastLoginAt;
}
