package kh.edu.istad.platform.iam.application.result;

import java.util.List;
import java.util.Set;

public record ClientSummaryResult(
    String id,
    String clientId,
    String clientName,
    String type, // "confidential" | "public"
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
