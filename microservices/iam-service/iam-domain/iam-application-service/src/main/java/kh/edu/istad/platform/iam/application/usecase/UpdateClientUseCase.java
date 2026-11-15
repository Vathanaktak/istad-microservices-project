package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.command.UpdateClientCommand;
import kh.edu.istad.platform.iam.application.port.out.ClientManagementPort;
import kh.edu.istad.platform.iam.application.result.ClientSummaryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateClientUseCase {
    private final ClientManagementPort clientPort;

    public ClientSummaryResult execute(String id, UpdateClientCommand cmd) {
        return clientPort.updateClient(id, cmd);
    }
}
