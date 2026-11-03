package kh.edu.istad.platform.order.client.httpClient;

import kh.edu.istad.platform.order.domain.dto.GetCustomerResult;
import org.springframework.security.oauth2.client.annotation.ClientRegistrationId;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@HttpExchange("/api/customers")
@ClientRegistrationId("customer-client")
public interface CustomerHttpClient {
    @GetExchange
    @ClientRegistrationId("customer-client")
    Flux<GetCustomerResult> getCustomers();

    @GetExchange("/{customerId}")
    @ClientRegistrationId("customer-client")
    Mono<GetCustomerResult> getCustomerById(@PathVariable UUID customerId);
}
