package kh.edu.istad.platform.order.client;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;


@Configuration
@Profile("!hardcoded")
public class EurekaClientConfig {

    /** @LoadBalanced = every request made by clients built from this builder resolves the host name through Eureka. */
    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    // "customer-service" is NOT a DNS name. It is the spring.application.name registered in Eureka.
    @Bean
    public RestClient customerRestClient(@LoadBalanced RestClient.Builder builder) {
        return build(builder, "http://customer-service");
    }

    @Bean
    public RestClient businessRestClient(@LoadBalanced RestClient.Builder builder) {
        return build(builder, "http://business-service");
    }

    @Bean
    public RestClient paymentRestClient(@LoadBalanced RestClient.Builder builder) {
        return build(builder, "http://payment-service");
    }

    private static RestClient build(RestClient.Builder builder, String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(5));
        return builder.clone().baseUrl(baseUrl).requestFactory(factory).build();
    }

}
