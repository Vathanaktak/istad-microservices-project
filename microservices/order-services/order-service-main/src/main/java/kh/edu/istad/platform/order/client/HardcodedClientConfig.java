package kh.edu.istad.platform.order.client;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;


@Configuration
@Profile("hardcoded")
public class HardcodedClientConfig {

    private static final String CUSTOMER_URL= "http://localhost:9091";
    private static final String BUSINESS_URL= "http://localhost:9090";
    private static final String PAYMENT_URL= "http://localhost:9093";

    @Bean
    public RestClient customerRestClient(){
        return build(CUSTOMER_URL);
    }
    @Bean
    public RestClient businessRestClient() {
        return build(BUSINESS_URL);
    }
    @Bean
    public RestClient paymentRestClient(){
        return build(PAYMENT_URL);
    }

    private static RestClient build(String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(5));
        return RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

}
