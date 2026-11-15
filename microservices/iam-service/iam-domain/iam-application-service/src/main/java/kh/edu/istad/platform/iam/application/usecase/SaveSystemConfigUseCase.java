package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.port.out.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SaveSystemConfigUseCase {
    private final SystemConfigRepository repository;

    public void execute(String configJson, String updatedBy) {
        repository.saveConfigJson(configJson, updatedBy);
    }
}
