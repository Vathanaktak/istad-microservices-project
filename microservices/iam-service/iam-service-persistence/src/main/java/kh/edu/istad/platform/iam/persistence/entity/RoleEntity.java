package kh.edu.istad.platform.iam.persistence.entity;
import jakarta.persistence.*; import lombok.*; import java.time.*; import java.util.*; import org.hibernate.annotations.SQLRestriction;
@Entity @Table(name = "roles") @SQLRestriction("is_deleted = false")
@Getter @Setter
public class RoleEntity extends BaseEntity {
    private String name;
    private String description;
    @Column(name = "is_system") private boolean system;
    @Column(name = "is_active") private boolean active;
}
