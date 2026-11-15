package kh.edu.istad.platform.iam.rest.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public final class ClientDtos {
    private ClientDtos() {}

    public record CreateClientRequest(
        @NotBlank String clientId,
        String clientName,
        String type,
        Set<String> grantTypes,
        Set<String> redirectUris,
        Set<String> postLogoutRedirectUris,
        Set<String> scopes,
        boolean requireProofKey,
        boolean requireAuthorizationConsent,
        int accessTokenMinutes,
        int refreshTokenHours
    ) {}

    public record UpdateClientRequest(
        String clientName,
        String type,
        Set<String> grantTypes,
        Set<String> redirectUris,
        Set<String> postLogoutRedirectUris,
        Set<String> scopes,
        boolean requireProofKey,
        boolean requireAuthorizationConsent,
        int accessTokenMinutes,
        int refreshTokenHours
    ) {}

    public record ClientResponse(
        String id,
        String clientId,
        String clientName,
        String type,
        Set<String> grantTypes,
        Set<String> authenticationMethods,
        Set<String> redirectUris,
        Set<String> postLogoutRedirectUris,
        Set<String> scopes,
        boolean requireProofKey,
        boolean requireAuthorizationConsent,
        int accessTokenMinutes,
        int refreshTokenHours
    ) {}

    public record ClientCreatedResponse(ClientResponse client, String secret) {}
    public record SecretResponse(String secret) {}
}
