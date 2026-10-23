package kh.edu.istad.platform.order.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import kh.edu.istad.platform.order.domain.exception.ServiceUnavailableException;
import kh.edu.istad.platform.order.domain.port.out.BusinessClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Optional;
import java.util.UUID;

@Component
public class BusinessRestClientAdapter implements BusinessClient {

    private static final String SERVICE_NAME = "business-service";

    private final RestClient restClient;

    public BusinessRestClientAdapter(@Qualifier("businessRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Optional<BusinessInfo> getBusiness(UUID businessId) {
        try {
            BusinessResponse body = restClient.get()
                    .uri("/api/businesses/{id}", businessId)
                    .retrieve()
                    .body(BusinessResponse.class);

            if (body == null) {
                return Optional.empty();
            }
            return Optional.of(new BusinessInfo(body.businessId(), body.displayName(), body.active()));

        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw new ServiceUnavailableException(SERVICE_NAME, ex);
        } catch (ResourceAccessException | IllegalStateException ex) {
            throw new ServiceUnavailableException(SERVICE_NAME, ex);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BusinessResponse(UUID businessId, String displayName, boolean active) {
    }
}