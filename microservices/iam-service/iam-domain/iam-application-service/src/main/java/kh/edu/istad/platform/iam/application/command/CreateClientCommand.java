package kh.edu.istad.platform.iam.application.command;

import java.util.Set;

public record CreateClientCommand(
    String clientId,
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
