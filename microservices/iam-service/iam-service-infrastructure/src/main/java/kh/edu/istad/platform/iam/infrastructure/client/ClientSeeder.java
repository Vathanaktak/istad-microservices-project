package kh.edu.istad.platform.iam.infrastructure.client;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

/**
 * Seeds the built-in clients:
 *  1. the client whose client_credentials token (scope client.create) is the DCR initial access token;
 *  2. "iam-web", the public Authorization-Code + PKCE client used by the React frontend (iam-service-web).
 */
@Component
@RequiredArgsConstructor
public class ClientSeeder implements ApplicationRunner {
    private final RegisteredClientRepository clients;
    private final PasswordEncoder encoder;
    @Value("${iam.dcr.client-id:iam-dcr-initial}") private String clientId;
    @Value("${iam.dcr.client-secret}") private String clientSecret;
    @Value("${iam.web.client-id:iam-web}") private String webClientId;
    @Value("${iam.web.redirect-uris:http://localhost:9000/auth/callback,http://localhost:5173/auth/callback}") private String webRedirectUris;

    @Override
    public void run(ApplicationArguments args) {
        seedDcrClient();
        seedWebClient();
    }

    private void seedDcrClient() {
        if (clients.findByClientId(clientId) != null) return;
        clients.save(RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId(clientId).clientName("Dynamic Client Registration (initial access)")
            .clientSecret(encoder.encode(clientSecret))
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .scope("client.create")
            .build());
    }

    private void seedWebClient() {
        if (clients.findByClientId(webClientId) != null) return;
        RegisteredClient.Builder b = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId(webClientId).clientName("IAM Web Console")
            .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)          // public SPA client: PKCE instead of a secret
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .scope(OidcScopes.OPENID).scope(OidcScopes.PROFILE).scope(OidcScopes.EMAIL)
            .scope(OidcScopes.PHONE).scope(OidcScopes.ADDRESS)
            .clientSettings(ClientSettings.builder().requireProofKey(true).requireAuthorizationConsent(false).build())
            .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(30))
                .refreshTokenTimeToLive(Duration.ofDays(7))
                .reuseRefreshTokens(false)                                         // required for public clients (rotation)
                .build());
        Arrays.stream(webRedirectUris.split(",")).map(String::trim).filter(s -> !s.isEmpty()).forEach(b::redirectUri);
        clients.save(b.build());
    }
}
