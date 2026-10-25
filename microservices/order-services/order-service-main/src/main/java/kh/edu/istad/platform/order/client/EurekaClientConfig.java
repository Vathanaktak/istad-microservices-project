package kh.edu.istad.platform.order.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class EurekaClientConfig {

    @Bean
    @Primary
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    @LoadBalanced
    @Qualifier("loadBalancedRestClientBuilder")
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient customerRestClient(@Qualifier("loadBalancedRestClientBuilder") RestClient.Builder builder) {
        return build(builder, "http://customer-service");
    }

    @Bean
    public RestClient businessRestClient(@Qualifier("loadBalancedRestClientBuilder") RestClient.Builder builder) {
        return build(builder, "http://business-service");
    }

    @Bean
    public RestClient paymentRestClient(@Qualifier("loadBalancedRestClientBuilder") RestClient.Builder builder) {
        return build(builder, "http://payment-service");
    }

    private static RestClient build(RestClient.Builder builder, String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(5));
        return builder.clone().baseUrl(baseUrl).requestFactory(factory).build();
    }

}
