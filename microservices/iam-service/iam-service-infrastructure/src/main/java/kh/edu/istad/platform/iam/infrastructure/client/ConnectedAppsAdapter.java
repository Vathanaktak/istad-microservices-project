package kh.edu.istad.platform.iam.infrastructure.client;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.usecase.*; import kh.edu.istad.platform.iam.domain.model.IdentityProvider; import kh.edu.istad.platform.iam.domain.exception.*; import lombok.RequiredArgsConstructor; import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component @RequiredArgsConstructor
public class ConnectedAppsAdapter implements ClientConsentPort {
    private final RegisteredClientRepository clients;
    private final OAuth2AuthorizationConsentService consents;
    private final JdbcTemplate jdbc;

    @Override
    public Optional<ClientInfo> findClient(String clientId) {
        RegisteredClient c = clients.findByClientId(clientId);
        return c == null ? Optional.empty() :
                Optional.of(
                        new ClientInfo(c.getClientId(), c.getClientName())
                );
    }

    @Override
    public List<ConnectedAppResult> listConnectedApps(String principalName) {
        List<ConnectedAppResult> out = new ArrayList<>();
        for (Map<String, Object> row : jdbc.queryForList(
                "select registered_client_id, authorities from oauth2_authorization_consent where principal_name = ?", principalName)) {
            RegisteredClient c = clients.findById((String) row.get("registered_client_id"));
            if (c == null) continue;
            Set<String> scopes = Arrays.stream(((String) row.get("authorities")).split(","))
                .filter(s -> s.startsWith("SCOPE_")).map(s -> s.substring(6)).collect(Collectors.toCollection(TreeSet::new));
            out.add(new ConnectedAppResult(c.getClientId(), c.getClientName(), scopes));
        }
        return out;
    }

    @Override
    public void revoke(String principalName, String clientId) {
        RegisteredClient c = clients.findByClientId(clientId);
        if (c == null) return;
        OAuth2AuthorizationConsent consent = consents.findById(c.getId(), principalName);
        if (consent != null) consents.remove(consent);
        jdbc.update("delete from oauth2_authorization where registered_client_id = ? and principal_name = ?", c.getId(), principalName);
    }
}
