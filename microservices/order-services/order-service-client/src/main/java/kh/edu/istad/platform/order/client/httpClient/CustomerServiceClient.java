//package kh.edu.istad.platform.order.client.httpClient;
//
//import kh.edu.istad.platform.order.domain.dto.GetCustomerResult;
//import kh.edu.istad.platform.order.domain.dto.QueryCustomerResult;
//import org.springframework.security.oauth2.client.annotation.ClientRegistrationId;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.service.annotation.GetExchange;
//import org.springframework.web.service.annotation.HttpExchange;
//import org.springframework.web.service.annotation.PostExchange;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//@HttpExchange("api/customers")
//@ClientRegistrationId("customer-client")
//public interface CustomerServiceClient {
//
//    @GetExchange
//    Flux<GetCustomerResult> getCustomers();
//
//    @GetExchange
//    Flux<QueryCustomerResult> getCustomerName();
//
//    @GetExchange("/{customerId}")
//    Mono<QueryCustomerResult> getCustomerById(@PathVariable("customerId") String customerId);
//
//    @PostExchange
//    Mono<Void> createCustomer(@RequestBody QueryCustomerResult request);
//
//}
