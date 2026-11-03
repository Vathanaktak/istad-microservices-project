package kh.edu.istad.platform.order.client.adapter;

import kh.edu.istad.platform.order.client.httpClient.CustomerHttpClient;
import kh.edu.istad.platform.order.domain.dto.GetCustomerResult;
import kh.edu.istad.platform.order.domain.dto.QueryCustomerResult;
import kh.edu.istad.platform.order.domain.port.out.client.CustomerServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomerServiceClientAdapter implements CustomerServiceClient {
    private final CustomerHttpClient http;

    public Flux<GetCustomerResult> getCustomers() { return http.getCustomers(); }
    public Mono<GetCustomerResult> getCustomerById(UUID id) { return http.getCustomerById(id); }
}