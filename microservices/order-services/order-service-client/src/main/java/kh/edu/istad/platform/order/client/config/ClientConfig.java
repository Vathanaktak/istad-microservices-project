package kh.edu.istad.platform.order.client.config;

import kh.edu.istad.platform.order.client.exception.DownStreamServiceException;
import kh.edu.istad.platform.order.client.httpClient.BusinessHttpClient;
import kh.edu.istad.platform.order.client.httpClient.CustomerHttpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.reactive.function.client.support.WebClientHttpServiceGroupConfigurer;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.service.registry.HttpServiceGroup;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
//@RequiredArgsConstructor
@ImportHttpServices(
        group = "customer-service",
        types = {CustomerHttpClient.class,},
        clientType = HttpServiceGroup.ClientType.WEB_CLIENT)
@ImportHttpServices(
        group = "business-service",
        types = {BusinessHttpClient.class,},
        clientType = HttpServiceGroup.ClientType.WEB_CLIENT
)
public class ClientConfig {

    public WebClientHttpServiceGroupConfigurer webClientHttpServiceGroupConfigurer(){
        return groups -> {
            groups.filterByName("customer-service")
                    .forEachClient(
                            ((group, clientBuilder) ->
                                    clientBuilder
                                            .defaultHeader("Content-Type", "application/json")
                                            .defaultStatusHandler(
                                                    HttpStatusCode::isError,
                                                    response -> {
                                                       return response
                                                                .createException()
                                                                .map(ex -> new DownStreamServiceException(ex.getMessage(),"customer-service is down"));
                                                    }
                                            ))
                    );
        };
    }

//
//    private final WebClient.Builder webClientBuilder;
//
//    @Bean
//    public CustomerHttpClient customerHttpClient(){
//
////        WebClient customer = webClientBuilder
////                .baseUrl("http://customer-service")
////                .build();
////
////        HttpServiceProxyFactory factory
////                = HttpServiceProxyFactory
////                .builderFor(WebClientAdapter.create(customer))
////                .build();
////        return factory.createClient(CustomerHttpClient.class);
//        return createClient("http://customer-service",CustomerHttpClient.class);
//    }
//
//
//
//    private <T> T createClient(String url,Class<T> type){
//        WebClient webClient = webClientBuilder
//                .baseUrl(url)
//                .build();
// /*
// How to use HttpServiceProxyFactory to create a client for a given type using a WebClient instance.
//        HttpServiceProxyFactory factory = HttpServiceProxyFactory
//                .builderFor(WebClientAdapter.create(webClient))
//                .build();
//        return factory.createClient(type);
//     */
//        // Use the createClient method to create a client for the given type using the WebClient instance.
//        return  createClient(webClient, type);
//  }
//
//    private <T> T createClient(WebClient client,Class<T> type){
//        HttpServiceProxyFactory factory = HttpServiceProxyFactory
//                .builderFor(WebClientAdapter.create(client))
//                .build();
//        return factory.createClient(type);
//    }

}
