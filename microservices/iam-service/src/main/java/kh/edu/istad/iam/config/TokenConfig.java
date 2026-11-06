package kh.edu.istad.iam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.security.Principal;

@Configuration
public class TokenConfig {

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {


        return context -> {

            SecurityContext cn = SecurityContextHolder.getContext();
            Authentication authentication = cn.getAuthentication();
            if(authentication == null) return;
            var principal = authentication.getPrincipal();

            if(principal instanceof UserDetails user){
                if (OAuth2ParameterNames.ACCESS_TOKEN.equals(context.getTokenType().getValue())) {
                    context
                            .getClaims()
                            .claims(claim -> {
                                claim.put("preferred_username",user.getUsername());
                                claim.put("authorities",user.getAuthorities());
                            });
                }
            }

        };
    }

}
