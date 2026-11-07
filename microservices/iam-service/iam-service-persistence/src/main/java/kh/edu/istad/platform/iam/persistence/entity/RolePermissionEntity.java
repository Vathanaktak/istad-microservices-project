package kh.edu.istad.platform.iam.persistence.entity;
import jakarta.persistence.*; import lombok.*; import java.time.*; import java.util.*; import org.hibernate.annotations.*;
@Entity @Table(name = "role_permissions") @Getter @Setter
public class RolePermissionEntity extends AssignmentEntity {
    @Column(name = "role_id", nullable = false) private Long roleId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "permission_id") private PermissionEntity permission;
}
