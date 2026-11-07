package kh.edu.istad.platform.iam.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/** Common audit, soft-delete and optimistic-lock state shared by all aggregates. */
@Getter
@Setter
public abstract class Auditable {
    private Long id;
    private UUID uuid = UUID.randomUUID();
    private boolean deleted;
    private Instant deletedAt;
    private String deletedBy;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;
    private Long version;

    public void softDelete(String actor) {
        this.deleted = true;
        this.deletedAt = Instant.now();
        this.deletedBy = actor;
    }
}
