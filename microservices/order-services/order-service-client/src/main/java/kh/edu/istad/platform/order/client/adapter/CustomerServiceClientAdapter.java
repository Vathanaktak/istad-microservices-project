package kh.edu.istad.platform.order.client.adapter;
import kh.edu.istad.platform.order.domain.dto.GetCustomerResult;
import kh.edu.istad.platform.order.domain.port.out.client.CustomerServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomerServiceClientAdapter implements CustomerServiceClient {

    private final WebClient.Builder webClientConfig;

    public record getCustomer(
            UUID customerId,
            String username,
            String familyName,
            String givenName,
            String email,
            String phoneNumber
    ){}

    @Override
    public Flux<GetCustomerResult> getCustomerName() {

        WebClient client = webClientConfig
                .baseUrl("http://customer-service")
                .build();
        var response = client
                .get()
                .uri("/api/customers")
                .retrieve()
                .bodyToFlux(getCustomer.class);

        return response.map(
                cus ->
                        new GetCustomerResult(
                                cus.customerId,
                                cus.username,
                                cus.familyName,
                                cus.givenName,
                                cus.email,
                                cus.phoneNumber
                        )
        );
    }
}
