package kh.edu.istad.platform.iam.persistence.entity;
import jakarta.persistence.*; import lombok.*; import java.time.*; import java.util.*; import org.hibernate.annotations.SQLRestriction;
@Entity @Table(name = "user_roles") @Getter @Setter
public class UserRoleEntity extends AssignmentEntity {
    @Column(name = "user_id", nullable = false) private Long userId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "role_id") private RoleEntity role;
}
