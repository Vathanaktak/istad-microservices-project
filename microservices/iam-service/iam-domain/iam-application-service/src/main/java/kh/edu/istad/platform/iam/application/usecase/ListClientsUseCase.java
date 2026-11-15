package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.port.out.ClientManagementPort;
import kh.edu.istad.platform.iam.application.result.ClientSummaryResult;
import kh.edu.istad.platform.iam.application.result.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListClientsUseCase {
    private final ClientManagementPort clientPort;

    public PageResult<ClientSummaryResult> execute(String query, int page, int size) {
        return clientPort.listClients(query, page, size);
    }
}
