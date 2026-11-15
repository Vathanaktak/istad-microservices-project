package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.command.CreateClientCommand;
import kh.edu.istad.platform.iam.application.port.out.ClientManagementPort;
import kh.edu.istad.platform.iam.application.result.ClientCreatedResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateClientUseCase {
    private final ClientManagementPort clientPort;

    public ClientCreatedResult execute(CreateClientCommand cmd) {
        return clientPort.createClient(cmd);
    }
}
