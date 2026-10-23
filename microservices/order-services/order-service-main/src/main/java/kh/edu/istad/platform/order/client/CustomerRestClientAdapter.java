package kh.edu.istad.platform.order.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import kh.edu.istad.platform.order.domain.exception.ServiceUnavailableException;
import kh.edu.istad.platform.order.domain.port.out.CustomerClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Optional;
import java.util.UUID;

@Component
public class CustomerRestClientAdapter implements CustomerClient {

    private static final String SERVICE_NAME = "customer service";
    private final RestClient restClient;

    public CustomerRestClientAdapter(@Qualifier("customerRestClient") RestClient restClient) {
        this.restClient = restClient;
    }


    @Override
    public Optional<CustomerInfo> getCustomer(UUID customerId) {
        try {
            CustomerResponse body = restClient.get()
                    .uri("/api/v1/customers/{id}", customerId)
                    .retrieve()
                    .body(CustomerResponse.class);

            if (body == null) {
                return Optional.empty();
            }
            String fullName = (body.givenName() + " " + body.familyName()).trim();
            return Optional.of(new CustomerInfo(body.customerId(), fullName, "ACTIVE".equals(body.status())));

        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                return Optional.empty();                      // customer simply does not exist
            }
            throw new ServiceUnavailableException(SERVICE_NAME, ex);   // 5xx or other errors
        } catch (ResourceAccessException | IllegalStateException ex) {
            // ResourceAccessException  = connection refused / timeout (hard-coded mode or dead instance)
            // IllegalStateException    = "No instances available for customer-service" (Eureka mode)
            throw new ServiceUnavailableException(SERVICE_NAME, ex);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CustomerResponse(UUID customerId, String familyName, String givenName, String status) {
    }
}
