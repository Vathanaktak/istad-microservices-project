package kh.edu.istad.platform.iam.application.port.out;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*;
import java.time.*; import java.util.*;
/** Read/revoke OAuth2 clients and consents without exposing Spring Authorization Server types. */
public interface ClientConsentPort {
    record ClientInfo(String clientId, String clientName) {}
    Optional<ClientInfo> findClient(String clientId);
    List<ConnectedAppResult> listConnectedApps(String principalName);
    void revoke(String principalName, String clientId);
}
