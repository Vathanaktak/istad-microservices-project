package kh.edu.istad.platform.iam.persistence.entity;
import jakarta.persistence.*; import lombok.*; import java.time.*; import java.util.*; import org.hibernate.annotations.*;
@Entity @Table(name = "user_permissions") @Getter @Setter
public class UserPermissionEntity extends AssignmentEntity {
    @Column(name = "user_id", nullable = false) private Long userId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "permission_id") private PermissionEntity permission;
    @Enumerated(EnumType.STRING) private kh.edu.istad.platform.iam.domain.model.Effect effect;
}
