package kh.edu.istad.platform.order.client.adapter;

import kh.edu.istad.platform.order.domain.dto.GetCustomerResult;
import kh.edu.istad.platform.order.domain.dto.QueryCustomerResult;
import kh.edu.istad.platform.order.domain.port.out.client.CustomerServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class CustomerServiceClientAdapter implements CustomerServiceClient {

    private final kh.edu.istad.platform.order.client.httpClient.CustomerServiceClient customerServiceClient;

    @Override
    public Flux<GetCustomerResult> getCustomerName() {
        return customerServiceClient.getCustomers();
    }

    @Override
    public Flux<QueryCustomerResult> queryCustomers() {
        return customerServiceClient.getCustomerName();
    }
}
