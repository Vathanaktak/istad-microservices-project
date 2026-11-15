package kh.edu.istad.platform.iam.infrastructure.config;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.usecase.*; import kh.edu.istad.platform.iam.domain.model.IdentityProvider; import kh.edu.istad.platform.iam.domain.exception.*; import lombok.RequiredArgsConstructor; import java.util.*;
import kh.edu.istad.platform.iam.infrastructure.federation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** Web login chain: form login, social login (Google/GitHub/Facebook), Telegram widget. */
@Configuration
public class DefaultSecurityConfig {

    @Bean PasswordEncoder passwordEncoder() { return PasswordEncoderFactories.createDelegatingPasswordEncoder(); }

    @Bean @Order(3)
    SecurityFilterChain defaultChain(HttpSecurity http, FederatedOAuth2UserService federated,
            FindOrProvisionFederatedUserUseCase provision, LinkIdentityUseCase link, LoadUserForAuthenticationUseCase load,
            RecordLoginAttemptUseCase recordAttempt,
            @Value("${iam.telegram.bot-token:}") String botToken) throws Exception {
        // The React SPA is served from this origin. It owns the /login, /register and /oauth2/consent screens and posts
        // plain HTML forms back to Spring, so CSRF is a double-submit cookie (XSRF-TOKEN) that the SPA copies into "_csrf".
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName(null);                      // load eagerly so the cookie is always issued
        http.csrf(c -> c
                .ignoringRequestMatchers("/logout")
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(csrfHandler))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/consent-info").authenticated()           // needs the IAM browser session
                .anyRequest().permitAll())                                  // SPA shell + static assets; data is guarded by the API chain
            .formLogin(f -> f.loginPage("/login"))
            .oauth2Login(o -> o.loginPage("/login").userInfoEndpoint(u -> u.userService(federated)))
            .logout(l -> l
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID", "XSRF-TOKEN"))
            .addFilterBefore(new TelegramLoginFilter(botToken, provision, link, load, recordAttempt), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
