package kh.edu.istad.platform.iam.application.port.out;

import java.util.Optional;

public interface SystemConfigRepository {
    Optional<String> findConfigJson();
    void saveConfigJson(String configJson, String updatedBy);
}
