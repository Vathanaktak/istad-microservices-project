package kh.edu.istad.platform.iam.infrastructure.adapter;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import kh.edu.istad.platform.iam.application.command.CreateClientCommand;
import kh.edu.istad.platform.iam.application.command.UpdateClientCommand;
import kh.edu.istad.platform.iam.application.port.out.ClientManagementPort;
import kh.edu.istad.platform.iam.application.result.ClientCreatedResult;
import kh.edu.istad.platform.iam.application.result.ClientSummaryResult;
import kh.edu.istad.platform.iam.application.result.PageResult;
import kh.edu.istad.platform.iam.domain.exception.ConflictException;
import kh.edu.istad.platform.iam.domain.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientManagementAdapter implements ClientManagementPort {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final RegisteredClientRepository clients;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbc;

    @Override
    public PageResult<ClientSummaryResult> listClients(String query, int page, int size) {
        String baseSql = "FROM oauth2_registered_client";
        List<Object> args = new ArrayList<>();
        if (query != null && !query.isBlank()) {
            baseSql += " WHERE LOWER(client_id) LIKE ? OR LOWER(client_name) LIKE ?";
            String like = "%" + query.toLowerCase(Locale.ROOT) + "%";
            args.add(like);
            args.add(like);
        }
        Long total = jdbc.queryForObject("SELECT COUNT(*) " + baseSql, Long.class, args.toArray());
        long count = total != null ? total : 0;

        int limit = Math.min(Math.max(size, 1), 100);
        int offset = Math.max(page, 0) * limit;
        args.add(limit);
        args.add(offset);
        List<String> ids = jdbc.queryForList("SELECT id " + baseSql + " ORDER BY client_id ASC LIMIT ? OFFSET ?", String.class, args.toArray());

        List<ClientSummaryResult> results = ids.stream()
            .map(clients::findById)
            .filter(Objects::nonNull)
            .map(this::toResult)
            .toList();

        int totalPages = (int) Math.ceil((double) count / limit);
        return new PageResult<>(results, page, limit, count, totalPages);
    }

    @Override
    public ClientSummaryResult getClient(String id) {
        RegisteredClient c = clients.findById(id);
        if (c == null) c = clients.findByClientId(id);
        if (c == null) throw NotFoundException.of("Client", id);
        return toResult(c);
    }

    @Override
    public ClientCreatedResult createClient(CreateClientCommand cmd) {
        if (clients.findByClientId(cmd.clientId()) != null) {
            throw new ConflictException("client-id-exists", "Client with id '" + cmd.clientId() + "' already exists");
        }
        String id = UUID.randomUUID().toString();
        boolean isPublic = "public".equalsIgnoreCase(cmd.type());
        String secret = isPublic ? null : randomSecret();

        RegisteredClient.Builder b = RegisteredClient.withId(id)
            .clientId(cmd.clientId())
            .clientName(cmd.clientName() != null ? cmd.clientName() : cmd.clientId());

        if (isPublic) {
            b.clientAuthenticationMethod(ClientAuthenticationMethod.NONE);
        } else {
            b.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
             .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
             .clientSecret(passwordEncoder.encode(secret));
        }

        if (cmd.grantTypes() != null) {
            cmd.grantTypes().forEach(g -> b.authorizationGrantType(new AuthorizationGrantType(g)));
        }
        if (cmd.redirectUris() != null) {
            cmd.redirectUris().forEach(b::redirectUri);
        }
        if (cmd.postLogoutRedirectUris() != null) {
            cmd.postLogoutRedirectUris().forEach(b::postLogoutRedirectUri);
        }
        if (cmd.scopes() != null) {
            cmd.scopes().forEach(b::scope);
        }

        b.clientSettings(ClientSettings.builder()
            .requireProofKey(cmd.requireProofKey())
            .requireAuthorizationConsent(cmd.requireAuthorizationConsent())
            .build());

        int accessMinutes = cmd.accessTokenMinutes() > 0 ? cmd.accessTokenMinutes() : 15;
        int refreshHours = cmd.refreshTokenHours() > 0 ? cmd.refreshTokenHours() : 24;
        b.tokenSettings(TokenSettings.builder()
            .accessTokenTimeToLive(Duration.ofMinutes(accessMinutes))
            .refreshTokenTimeToLive(Duration.ofHours(refreshHours))
            .reuseRefreshTokens(false)
            .build());

        RegisteredClient saved = b.build();
        clients.save(saved);
        return new ClientCreatedResult(toResult(saved), secret);
    }

    @Override
    public ClientSummaryResult updateClient(String id, UpdateClientCommand cmd) {
        RegisteredClient existing = clients.findById(id);
        if (existing == null) existing = clients.findByClientId(id);
        if (existing == null) throw NotFoundException.of("Client", id);

        RegisteredClient.Builder b = RegisteredClient.from(existing)
            .clientName(cmd.clientName());

        boolean isPublic = "public".equalsIgnoreCase(cmd.type());
        if (isPublic) {
            b.clientAuthenticationMethods(m -> {
                m.clear();
                m.add(ClientAuthenticationMethod.NONE);
            });
        } else {
            b.clientAuthenticationMethods(m -> {
                m.clear();
                m.add(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
                m.add(ClientAuthenticationMethod.CLIENT_SECRET_POST);
            });
        }

        if (cmd.grantTypes() != null) {
            b.authorizationGrantTypes(g -> {
                g.clear();
                cmd.grantTypes().forEach(gt -> g.add(new AuthorizationGrantType(gt)));
            });
        }
        if (cmd.redirectUris() != null) {
            b.redirectUris(u -> {
                u.clear();
                u.addAll(cmd.redirectUris());
            });
        }
        if (cmd.postLogoutRedirectUris() != null) {
            b.postLogoutRedirectUris(u -> {
                u.clear();
                u.addAll(cmd.postLogoutRedirectUris());
            });
        }
        if (cmd.scopes() != null) {
            b.scopes(s -> {
                s.clear();
                s.addAll(cmd.scopes());
            });
        }

        b.clientSettings(ClientSettings.builder()
            .requireProofKey(cmd.requireProofKey())
            .requireAuthorizationConsent(cmd.requireAuthorizationConsent())
            .build());

        int accessMinutes = cmd.accessTokenMinutes() > 0 ? cmd.accessTokenMinutes() : 15;
        int refreshHours = cmd.refreshTokenHours() > 0 ? cmd.refreshTokenHours() : 24;
        b.tokenSettings(TokenSettings.builder()
            .accessTokenTimeToLive(Duration.ofMinutes(accessMinutes))
            .refreshTokenTimeToLive(Duration.ofHours(refreshHours))
            .reuseRefreshTokens(false)
            .build());

        RegisteredClient updated = b.build();
        clients.save(updated);
        return toResult(updated);
    }

    @Override
    public void deleteClient(String id) {
        RegisteredClient c = clients.findById(id);
        if (c == null) c = clients.findByClientId(id);
        if (c == null) return;
        jdbc.update("DELETE FROM oauth2_authorization_consent WHERE registered_client_id = ?", c.getId());
        jdbc.update("DELETE FROM oauth2_authorization WHERE registered_client_id = ?", c.getId());
        jdbc.update("DELETE FROM oauth2_registered_client WHERE id = ?", c.getId());
    }

    @Override
    public String regenerateSecret(String id) {
        RegisteredClient c = clients.findById(id);
        if (c == null) c = clients.findByClientId(id);
        if (c == null) throw NotFoundException.of("Client", id);

        String secret = randomSecret();
        RegisteredClient updated = RegisteredClient.from(c)
            .clientSecret(passwordEncoder.encode(secret))
            .build();
        clients.save(updated);
        return secret;
    }

    private ClientSummaryResult toResult(RegisteredClient c) {
        Set<String> methods = c.getClientAuthenticationMethods().stream().map(ClientAuthenticationMethod::getValue).collect(Collectors.toSet());
        String type = methods.contains("none") ? "public" : "confidential";
        Set<String> grants = c.getAuthorizationGrantTypes().stream().map(AuthorizationGrantType::getValue).collect(Collectors.toSet());
        int accessMinutes = (int) c.getTokenSettings().getAccessTokenTimeToLive().toMinutes();
        int refreshHours = (int) c.getTokenSettings().getRefreshTokenTimeToLive().toHours();
        return new ClientSummaryResult(
            c.getId(),
            c.getClientId(),
            c.getClientName(),
            type,
            grants,
            methods,
            c.getRedirectUris(),
            c.getPostLogoutRedirectUris(),
            c.getScopes(),
            c.getClientSettings().isRequireProofKey(),
            c.getClientSettings().isRequireAuthorizationConsent(),
            accessMinutes,
            refreshHours
        );
    }

    private static String randomSecret() {
        byte[] b = new byte[32];
        RANDOM.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }
}
