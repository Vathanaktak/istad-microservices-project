package kh.edu.istad.platform.order.client.config;

import kh.edu.istad.platform.order.client.exception.DownStreamServiceException;
import kh.edu.istad.platform.order.client.httpClient.BusinessHttpClient;
import kh.edu.istad.platform.order.client.httpClient.CustomerHttpClient;
import kh.edu.istad.platform.order.client.httpClient.CustomerServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.support.WebClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.HttpServiceGroup;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(
        group = "customer-service",
        types = {CustomerHttpClient.class, CustomerServiceClient.class},
        clientType = HttpServiceGroup.ClientType.WEB_CLIENT
)
@ImportHttpServices(
        group = "business-service",
        types = {BusinessHttpClient.class},
        clientType = HttpServiceGroup.ClientType.WEB_CLIENT
)
public class ClientConfig {

    @Bean
    public WebClientHttpServiceGroupConfigurer webClientHttpServiceGroupConfigurer() {
        return groups -> {
            groups.filterByName("customer-service")
                    .forEachClient((group, clientBuilder) ->
                            clientBuilder
                                    .baseUrl("http://customer-service")
                                    .defaultHeader("Content-Type", "application/json")
                                    .defaultStatusHandler(
                                            HttpStatusCode::isError,
                                            response -> response
                                                    .createException()
                                                    .map(ex -> new DownStreamServiceException(ex.getMessage(), "customer-service is down"))
                                    )
                    );

            groups.filterByName("business-service")
                    .forEachClient((group, clientBuilder) ->
                            clientBuilder
                                    .baseUrl("http://business-service")
                                    .defaultHeader("Content-Type", "application/json")
                                    .defaultStatusHandler(
                                            HttpStatusCode::isError,
                                            response -> response
                                                    .createException()
                                                    .map(ex -> new DownStreamServiceException(ex.getMessage(), "business-service is down"))
                                    )
                    );
        };
    }
}
