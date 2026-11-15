package kh.edu.istad.platform.iam.persistence.adapter;

import java.time.Instant;
import java.util.Optional;
import kh.edu.istad.platform.iam.application.port.out.SystemConfigRepository;
import kh.edu.istad.platform.iam.persistence.entity.SystemConfigEntity;
import kh.edu.istad.platform.iam.persistence.repository.SystemConfigJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SystemConfigRepositoryAdapter implements SystemConfigRepository {
    private final SystemConfigJpaRepository jpa;

    @Override
    public Optional<String> findConfigJson() {
        return jpa.findFirstByOrderByIdDesc().map(SystemConfigEntity::getConfigJson);
    }

    @Override
    public void saveConfigJson(String configJson, String updatedBy) {
        SystemConfigEntity e = jpa.findFirstByOrderByIdDesc().orElseGet(SystemConfigEntity::new);
        e.setConfigJson(configJson);
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(updatedBy != null ? updatedBy : "system");
        jpa.saveAndFlush(e);
    }
}
