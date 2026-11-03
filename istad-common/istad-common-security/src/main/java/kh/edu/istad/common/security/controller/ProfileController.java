package kh.edu.istad.common.security.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProfileController {

    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('SCOPE_read')")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(Map.of(
                "subject", jwt.getSubject(),
                "claims", jwt.getClaims(),
                "message", "Authenticated user profile from JWT token"
        ));
    }
}
