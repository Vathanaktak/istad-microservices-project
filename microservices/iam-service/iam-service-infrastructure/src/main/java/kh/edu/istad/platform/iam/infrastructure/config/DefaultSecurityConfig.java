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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** Web login chain: form login, social login (Google/GitHub/Facebook), Telegram widget. */
@Configuration
public class DefaultSecurityConfig {

    @Bean PasswordEncoder passwordEncoder() { return PasswordEncoderFactories.createDelegatingPasswordEncoder(); }

    @Bean @Order(3)
    SecurityFilterChain defaultChain(HttpSecurity http, FederatedOAuth2UserService federated,
            FindOrProvisionFederatedUserUseCase provision, LinkIdentityUseCase link, LoadUserForAuthenticationUseCase load,
            @Value("${iam.telegram.bot-token:}") String botToken) throws Exception {
        http.authorizeHttpRequests(a -> a
                .requestMatchers("/login", "/login/telegram", "/register", "/css/**", "/js/**", "/images/**", "/error", "/favicon.ico").permitAll()
                .anyRequest().authenticated())
            .formLogin(f -> f.loginPage("/login"))
            .oauth2Login(o -> o.loginPage("/login").userInfoEndpoint(u -> u.userService(federated)))
            .logout(l -> l.logoutSuccessUrl("/login?logout"))
            .addFilterBefore(new TelegramLoginFilter(botToken, provision, link, load), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
