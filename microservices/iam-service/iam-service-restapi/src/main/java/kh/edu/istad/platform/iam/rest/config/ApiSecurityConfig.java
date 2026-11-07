package kh.edu.istad.platform.iam.rest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import kh.edu.istad.platform.iam.rest.error.Problems;
import java.util.*;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/** Stateless Bearer-JWT security for /api/**: authorities come from the token's roles + permissions claims. */
@Configuration
@EnableMethodSecurity
public class ApiSecurityConfig {

    @Bean @Order(2)
    SecurityFilterChain apiChain(HttpSecurity http, ObjectMapper om) throws Exception {
        http.securityMatcher("/api/**")
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a.requestMatchers("/api/auth/register").permitAll().anyRequest().authenticated())
            .oauth2ResourceServer(o -> o
                .jwt(j -> j.jwtAuthenticationConverter(jwtConverter()))
                .authenticationEntryPoint((req, res, e) -> Problems.write(res, om, HttpStatus.UNAUTHORIZED, "unauthorized", "Unauthorized", "A valid Bearer token is required"))
                .accessDeniedHandler((req, res, e) -> Problems.write(res, om, HttpStatus.FORBIDDEN, "forbidden", "Forbidden", "You do not have permission to perform this action")));
        return http.build();
    }

    static JwtAuthenticationConverter jwtConverter() {
        JwtAuthenticationConverter c = new JwtAuthenticationConverter();
        c.setJwtGrantedAuthoritiesConverter((Jwt jwt) -> {
            List<GrantedAuthority> out = new ArrayList<>();
            for (String r : claim(jwt, "roles")) out.add(new SimpleGrantedAuthority("ROLE_" + r));
            for (String p : claim(jwt, "permissions")) out.add(new SimpleGrantedAuthority(p));
            return out;
        });
        return c;
    }
    private static List<String> claim(Jwt jwt, String name) {
        List<String> v = jwt.getClaimAsStringList(name);
        return v == null ? List.of() : v;
    }
}
