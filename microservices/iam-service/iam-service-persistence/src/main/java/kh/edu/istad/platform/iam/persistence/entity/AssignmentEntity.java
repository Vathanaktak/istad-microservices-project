package kh.edu.istad.platform.iam.persistence.entity;
import jakarta.persistence.*; import lombok.*; import java.time.*; import java.util.*; import org.hibernate.annotations.*;
@MappedSuperclass @Getter @Setter
public abstract class AssignmentEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Instant assignedAt;
    private String assignedBy;
    private Instant expiresAt;
    private Instant revokedAt;
    private String revokedBy;
    private String reason;
}
