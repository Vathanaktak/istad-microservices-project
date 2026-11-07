package kh.edu.istad.platform.iam.infrastructure.client;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.usecase.*; import kh.edu.istad.platform.iam.domain.model.IdentityProvider; import kh.edu.istad.platform.iam.domain.exception.*; import lombok.RequiredArgsConstructor; import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;

/** Seeds the client whose client_credentials token (scope client.create) is the DCR initial access token. */
@Component @RequiredArgsConstructor
public class ClientSeeder implements ApplicationRunner {
    private final RegisteredClientRepository clients;
    private final PasswordEncoder encoder;
    @Value("${iam.dcr.client-id:iam-dcr-initial}") private String clientId;
    @Value("${iam.dcr.client-secret}") private String clientSecret;

    @Override
    public void run(ApplicationArguments args) {
        if (clients.findByClientId(clientId) != null) return;
        clients.save(RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId(clientId).clientName("Dynamic Client Registration (initial access)")
            .clientSecret(encoder.encode(clientSecret))
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .scope("client.create")
            .build());
    }
}
