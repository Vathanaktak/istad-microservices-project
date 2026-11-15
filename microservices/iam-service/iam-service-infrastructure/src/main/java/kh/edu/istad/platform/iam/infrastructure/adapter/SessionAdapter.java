package kh.edu.istad.platform.iam.infrastructure.adapter;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import kh.edu.istad.platform.iam.application.port.out.SessionPort;
import kh.edu.istad.platform.iam.application.result.SessionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionAdapter implements SessionPort {
    private final JdbcTemplate jdbc;
    private final OAuth2AuthorizationService authorizationService;

    @Override
    public List<SessionResult> listSessions(String principalName, String currentTokenId) {
        String sql = "SELECT id, registered_client_id, principal_name, " +
                     "COALESCE(authorization_code_issued_at, oidc_id_token_issued_at, access_token_issued_at, refresh_token_issued_at, now()) AS created_at, " +
                     "COALESCE(access_token_issued_at, refresh_token_issued_at, oidc_id_token_issued_at, now()) AS last_active_at " +
                     "FROM oauth2_authorization WHERE principal_name = ? ORDER BY access_token_issued_at DESC NULLS LAST";
        return jdbc.query(sql, (rs, rowNum) -> {
            String id = rs.getString("id");
            Timestamp createdAtTs = rs.getTimestamp("created_at");
            Timestamp lastActiveTs = rs.getTimestamp("last_active_at");
            Instant createdAt = createdAtTs != null ? createdAtTs.toInstant() : Instant.now();
            Instant lastActiveAt = lastActiveTs != null ? lastActiveTs.toInstant() : createdAt;
            boolean current = (currentTokenId != null && currentTokenId.equals(id)) || rowNum == 0;
            return new SessionResult(id, "Desktop Browser", current, "Chrome", "Desktop", "127.0.0.1", "Phnom Penh, KH", lastActiveAt, createdAt);
        }, principalName);
    }

    @Override
    public void terminateSession(String principalName, String sessionId) {
        var auth = authorizationService.findById(sessionId);
        if (auth != null && principalName.equals(auth.getPrincipalName())) {
            authorizationService.remove(auth);
        }
    }

    @Override
    public void terminateAllSessions(String principalName) {
        List<String> ids = jdbc.queryForList("SELECT id FROM oauth2_authorization WHERE principal_name = ?", String.class, principalName);
        for (String id : ids) {
            var auth = authorizationService.findById(id);
            if (auth != null) {
                authorizationService.remove(auth);
            }
        }
    }
}
