package kh.edu.istad.platform.iam.rest.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.Principal;
import java.util.Map;
import kh.edu.istad.platform.iam.application.usecase.GetSystemConfigUseCase;
import kh.edu.istad.platform.iam.application.usecase.SaveSystemConfigUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/config")
@RequiredArgsConstructor
public class SystemConfigController {
    private final GetSystemConfigUseCase getConfig;
    private final SaveSystemConfigUseCase saveConfig;
    private final ObjectMapper objectMapper;

    private static final String DEFAULT_CONFIG = """
        {
          "authentication": {
            "maxFailedAttempts": 5,
            "lockoutMinutes": 15,
            "allowRegistration": true,
            "allowSocialLogin": true,
            "requireVerifiedEmail": false
          },
          "password": {
            "minLength": 8,
            "expireDays": 0,
            "historyCount": 0,
            "requireUppercase": true,
            "requireLowercase": true,
            "requireDigit": true,
            "requireSymbol": true
          },
          "session": {
            "idleTimeoutMinutes": 30,
            "absoluteTimeoutHours": 24,
            "maxConcurrentSessions": 5
          },
          "token": {
            "accessTokenMinutes": 15,
            "refreshTokenHours": 24,
            "idTokenMinutes": 15,
            "authorizationCodeMinutes": 5
          }
        }
        """;

    @GetMapping
    @PreAuthorize("hasAuthority('system:read') or hasRole('ADMIN')")
    public Map<String, Object> get() throws Exception {
        String json = getConfig.execute().orElse(DEFAULT_CONFIG);
        return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
    }

    @PutMapping
    @PreAuthorize("hasAuthority('system:update') or hasRole('ADMIN')")
    public Map<String, Object> update(@RequestBody Map<String, Object> config, Principal principal) throws Exception {
        String json = objectMapper.writeValueAsString(config);
        saveConfig.execute(json, principal != null ? principal.getName() : "admin");
        return config;
    }
}
