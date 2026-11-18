package kh.edu.istad.platform.iam.application.port.out;

import java.util.List;
import kh.edu.istad.platform.iam.application.command.CreateClientCommand;
import kh.edu.istad.platform.iam.application.command.UpdateClientCommand;
import kh.edu.istad.platform.iam.application.result.ClientCreatedResult;
import kh.edu.istad.platform.iam.application.result.ClientSummaryResult;
import kh.edu.istad.platform.iam.application.result.PageResult;

public interface ClientManagementPort {
    PageResult<ClientSummaryResult> listClients(String query, int page, int size);
    ClientSummaryResult getClient(String id);
    ClientCreatedResult createClient(CreateClientCommand cmd);
    ClientSummaryResult updateClient(String id, UpdateClientCommand cmd);
    void deleteClient(String id);
    String regenerateSecret(String id);
}
