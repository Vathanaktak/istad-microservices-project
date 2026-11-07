package kh.edu.istad.platform.iam.infrastructure.config;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.usecase.*; import kh.edu.istad.platform.iam.domain.model.IdentityProvider; import kh.edu.istad.platform.iam.domain.exception.*; import lombok.RequiredArgsConstructor; import java.util.*;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import kh.edu.istad.platform.iam.infrastructure.oidc.OidcClaimsMapper;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.server.authorization.*;
import org.springframework.security.oauth2.server.authorization.client.*;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

@Configuration
public class AuthorizationServerConfig {

    @Bean @Order(1)
    SecurityFilterChain authorizationServerChain(HttpSecurity http, LoadUserForAuthenticationUseCase loadUser) throws Exception {
        OAuth2AuthorizationServerConfigurer asc = OAuth2AuthorizationServerConfigurer.authorizationServer();
        http.securityMatcher(asc.getEndpointsMatcher())
            .with(asc, as -> as
                .authorizationEndpoint(e -> e.consentPage("/oauth2/consent"))
                .oidc(oidc -> oidc
                    .clientRegistrationEndpoint(Customizer.withDefaults())            // Dynamic Client Registration
                    .userInfoEndpoint(u -> u.userInfoMapper(ctx -> new OidcUserInfo(OidcClaimsMapper.claims(
                        loadUser.execute(ctx.getAuthorization().getPrincipalName()), ctx.getAccessToken().getScopes()))))))
            .authorizeHttpRequests(a -> a.anyRequest().authenticated())
            .exceptionHandling(e -> e.defaultAuthenticationEntryPointFor(
                new LoginUrlAuthenticationEntryPoint("/login"), new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
            .oauth2ResourceServer(r -> r.jwt(Customizer.withDefaults()));            // userinfo + DCR bearer tokens
        return http.build();
    }

    // --- JDBC-backed stores (tables created by Liquibase 003-oauth2-schema.sql)
    @Bean RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbc) { return new JdbcRegisteredClientRepository(jdbc); }
    @Bean OAuth2AuthorizationService authorizationService(JdbcTemplate jdbc, RegisteredClientRepository clients) {
        return new JdbcOAuth2AuthorizationService(jdbc, clients);
    }
    @Bean OAuth2AuthorizationConsentService authorizationConsentService(JdbcTemplate jdbc, RegisteredClientRepository clients) {
        return new JdbcOAuth2AuthorizationConsentService(jdbc, clients);
    }

    @Bean AuthorizationServerSettings authorizationServerSettings(@Value("${iam.issuer}") String issuer) {
        return AuthorizationServerSettings.builder().issuer(issuer).build();
    }

    /** RSA key from env (PKCS#8 / X.509, base64) or an ephemeral one for local development. */
    @Bean JWKSource<SecurityContext> jwkSource(@Value("${iam.jwt.private-key:}") String privateKey,
                                               @Value("${iam.jwt.public-key:}") String publicKey) throws GeneralSecurityException {
        KeyPair pair;
        String kid;
        if (!privateKey.isBlank() && !publicKey.isBlank()) {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            pair = new KeyPair(
                kf.generatePublic(new X509EncodedKeySpec(Base64.getMimeDecoder().decode(publicKey))),
                kf.generatePrivate(new PKCS8EncodedKeySpec(Base64.getMimeDecoder().decode(privateKey))));
            kid = "iam-key-1";
        } else {
            KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
            g.initialize(2048);
            pair = g.generateKeyPair();
            kid = UUID.randomUUID().toString();
        }
        RSAKey rsa = new RSAKey.Builder((RSAPublicKey) pair.getPublic()).privateKey((RSAPrivateKey) pair.getPrivate()).keyID(kid).build();
        return new ImmutableJWKSet<>(new JWKSet(rsa));
    }

    @Bean JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }
}
