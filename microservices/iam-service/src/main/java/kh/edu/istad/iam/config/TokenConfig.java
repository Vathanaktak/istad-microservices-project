package kh.edu.istad.iam.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

@Configuration
@Slf4j
public class TokenConfig {

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            System.out.println("Htttttttt===========");

            Authentication authentication = context.getPrincipal();
            log.info("Authentication: {}",authentication);
            if(authentication == null){
                return;
            }

            var principle = authentication.getPrincipal();

            log.info("principle:{}",principle);
            if(principle instanceof UserDetails userDetails){
                if(OAuth2ParameterNames.ACCESS_TOKEN.equals(context.getTokenType().getValue())){
                    context.getClaims()
                            .claims(claim -> {
                                claim.put("preferred_username",userDetails.getUsername());
                                claim.put("authorities",userDetails.getAuthorities());
                            });
                }
            }


            if (context.getTokenType().getValue().equals("access_token")) {
                context.getClaims().claim("custom-claim", "custom-value");
            }
        };
    }

}
