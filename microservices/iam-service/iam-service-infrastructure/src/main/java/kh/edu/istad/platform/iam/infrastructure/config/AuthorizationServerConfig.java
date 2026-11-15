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
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

@Configuration
public class AuthorizationServerConfig {

    @Bean @Order(1)
    SecurityFilterChain authorizationServerChain(HttpSecurity http, LoadUserForAuthenticationUseCase loadUser) throws Exception {
        OAuth2AuthorizationServerConfigurer asc = OAuth2AuthorizationServerConfigurer.authorizationServer();
        // same cookie-based CSRF as the login chain: the React consent form posts to /oauth2/authorize with the XSRF-TOKEN value
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName(null);
        http.csrf(c -> c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).csrfTokenRequestHandler(csrfHandler));
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
        if (!privateKey.isBlank() && !publicKey.isBlank()) {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            pair = new KeyPair(
                kf.generatePublic(new X509EncodedKeySpec(Base64.getMimeDecoder().decode(publicKey))),
                kf.generatePrivate(new PKCS8EncodedKeySpec(Base64.getMimeDecoder().decode(privateKey))));
        } else {
            KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
            g.initialize(2048);
            pair = g.generateKeyPair();
        }
        // Derive a stable key ID from the SHA-256 thumbprint of the public key so that
        // token validators can cache JWKS by kid even across restarts using a fixed key.
        String kid = thumbprint((java.security.interfaces.RSAPublicKey) pair.getPublic());
        RSAKey rsa = new RSAKey.Builder((RSAPublicKey) pair.getPublic()).privateKey((RSAPrivateKey) pair.getPrivate()).keyID(kid).build();
        return new ImmutableJWKSet<>(new JWKSet(rsa));
    }

    /** SHA-256 thumbprint of the DER-encoded SubjectPublicKeyInfo, hex-encoded (first 16 chars). */
    private static String thumbprint(java.security.interfaces.RSAPublicKey pub) throws GeneralSecurityException {
        byte[] der = pub.getEncoded();                                      // X.509 SubjectPublicKeyInfo
        java.security.MessageDigest sha = java.security.MessageDigest.getInstance("SHA-256");
        byte[] digest = sha.digest(der);
        StringBuilder sb = new StringBuilder(64);
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.substring(0, 32);                                         // 128-bit prefix is sufficient for a kid
    }

    @Bean JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }
}
