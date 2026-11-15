package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.port.out.ClientManagementPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegenerateClientSecretUseCase {
    private final ClientManagementPort clientPort;

    public String execute(String id) {
        return clientPort.regenerateSecret(id);
    }
}
