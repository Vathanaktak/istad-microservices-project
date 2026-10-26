package kh.edu.istad.platform.order.domain.port.out.client;

import kh.edu.istad.platform.order.domain.dto.GetCustomerResult;
import reactor.core.publisher.Flux;

public interface CustomerServiceClient
{

    Flux<GetCustomerResult> getCustomerName();

}
