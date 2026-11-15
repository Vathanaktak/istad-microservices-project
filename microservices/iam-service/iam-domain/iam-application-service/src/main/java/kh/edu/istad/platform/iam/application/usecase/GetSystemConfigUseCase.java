package kh.edu.istad.platform.iam.application.usecase;

import java.util.Optional;
import kh.edu.istad.platform.iam.application.port.out.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetSystemConfigUseCase {
    private final SystemConfigRepository repository;

    public Optional<String> execute() {
        return repository.findConfigJson();
    }
}
