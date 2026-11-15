package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.port.out.ClientManagementPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteClientUseCase {
    private final ClientManagementPort clientPort;

    public void execute(String id) {
        clientPort.deleteClient(id);
    }
}
