package kh.edu.istad.platform.order.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.support.OAuth2WebClientHttpServiceGroupConfigurer;
import org.springframework.web.reactive.function.client.support.WebClientHttpServiceGroupConfigurer;

@Configuration
public class OAuth2ClientConfig {

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return new OAuth2AuthorizedClientManager() {
            @Override
            public OAuth2AuthorizedClient authorize(OAuth2AuthorizeRequest authorizeRequest) {
                System.out.println(">>> AUTHORIZING REQUEST: " + authorizeRequest.getClientRegistrationId());
                try {
                    OAuth2AuthorizedClient client = authorizedClientManager.authorize(authorizeRequest);
                    System.out.println(">>> AUTHORIZED RESULT: " + client);
                    return client;
                } catch (Exception e) {
                    System.out.println(">>> AUTHORIZE EXCEPTION: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
            }
        };
    }

    @Bean
    WebClientHttpServiceGroupConfigurer oauth2WebClientHttpServiceGroupConfigurer(OAuth2AuthorizedClientManager m) {
        System.out.println(">>> REGISTERING oauth2WebClientHttpServiceGroupConfigurer with manager: " + m);
        WebClientHttpServiceGroupConfigurer configurer = OAuth2WebClientHttpServiceGroupConfigurer.from(m);
        return groups -> {
            System.out.println(">>> RUNNING oauth2WebClientHttpServiceGroupConfigurer on groups!");
            configurer.configureGroups(groups);
            groups.forEachClient((group, client) -> client.filter((request, next) -> {
                System.out.println(">>> WEBCLIENT REQUEST TO: " + request.url());
                System.out.println(">>> REQUEST ATTRIBUTES: " + request.attributes().keySet());
                System.out.println(">>> REQUEST CLIENT_ID ATTR: " + request.attribute("org.springframework.security.oauth2.client.registration.ClientRegistration.CLIENT_REGISTRATION_ID"));
                System.out.println(">>> REQUEST HEADERS BEFORE: " + request.headers());
                return next.exchange(request).doOnNext(res -> {
                    System.out.println(">>> RESPONSE STATUS: " + res.statusCode());
                });
            }));
        };
    }
}
