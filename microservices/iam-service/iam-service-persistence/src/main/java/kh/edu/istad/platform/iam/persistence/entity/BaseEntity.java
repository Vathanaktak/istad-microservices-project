package kh.edu.istad.platform.iam.persistence.entity;
import jakarta.persistence.*; import lombok.*; import java.time.*; import java.util.*; import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
public abstract class BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;
    private Instant deletedAt;
    private String deletedBy;
    @CreatedDate @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @CreatedBy @Column(nullable = false, updatable = false)
    private String createdBy;
    @LastModifiedDate @Column(nullable = false)
    private Instant updatedAt;
    @LastModifiedBy @Column(nullable = false)
    private String updatedBy;
    @Version
    private Long version;
}
