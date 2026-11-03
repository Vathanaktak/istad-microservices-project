package kh.edu.istad.platform.order.client.config;

import kh.edu.istad.platform.order.client.exception.DownStreamServiceException;
import kh.edu.istad.platform.order.client.httpClient.BusinessHttpClient;
import kh.edu.istad.platform.order.client.httpClient.CustomerHttpClient;
import kh.edu.istad.platform.order.client.httpClient.CustomerServiceClient;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
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
    public WebClientHttpServiceGroupConfigurer clientConfigHttpServiceGroupConfigurer(
            ReactorLoadBalancerExchangeFilterFunction lbFunction) {
        System.out.println(">>> REGISTERING clientConfigHttpServiceGroupConfigurer");
        return groups -> {
            System.out.println(">>> RUNNING clientConfigHttpServiceGroupConfigurer on groups!");
            groups.forEachClient((group, clientBuilder) -> clientBuilder.filter(lbFunction));

            groups.filterByName("customer-service")
                    .forEachClient((group, clientBuilder) ->
                            clientBuilder
                                    .defaultHeader("Content-Type", "application/json")
                                    .defaultStatusHandler(
                                            HttpStatusCode::isError,
                                            response -> response
                                                    .createException()
                                                    .map(ex -> new DownStreamServiceException(
                                                            "customer-service returned " + ex.getStatusCode() + ": " + ex.getMessage(),
                                                            "customer-service"))
                                    )
                    );

            groups.filterByName("business-service")
                    .forEachClient((group, clientBuilder) ->
                            clientBuilder
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
