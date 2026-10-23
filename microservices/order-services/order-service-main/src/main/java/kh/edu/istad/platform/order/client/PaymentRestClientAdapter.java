package kh.edu.istad.platform.order.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import kh.edu.istad.platform.order.domain.exception.ServiceUnavailableException;
import kh.edu.istad.platform.order.domain.port.out.PaymentClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class PaymentRestClientAdapter implements PaymentClient {

    private static final String SERVICE_NAME = "payment-service";

    private final RestClient restClient;

    public PaymentRestClientAdapter(@Qualifier("paymentRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public PaymentInfo pay(UUID orderId, UUID customerId, BigDecimal amount) {
        try {
            PaymentResponse body = restClient.post()
                    .uri("/api/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new PaymentRequest(orderId, customerId, amount))
                    .retrieve()
                    .body(PaymentResponse.class);

            if (body == null) {
                return new PaymentInfo("FAILED", List.of("Empty response from payment-service"));
            }
            List<String> failures = body.failureMessages() == null ? List.of() : body.failureMessages();
            return new PaymentInfo(body.status(), failures);

        } catch (HttpClientErrorException ex) {
            // 4xx: payment-service understood the request but refused it
            return new PaymentInfo("FAILED", List.of(ex.getResponseBodyAsString()));
        } catch (RestClientResponseException ex) {
            // 5xx: payment-service is broken
            throw new ServiceUnavailableException(SERVICE_NAME, ex);
        } catch (ResourceAccessException | IllegalStateException ex) {
            throw new ServiceUnavailableException(SERVICE_NAME, ex);
        }
    }

    public record PaymentRequest(UUID orderId, UUID customerId, BigDecimal price) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PaymentResponse(String status, List<String> failureMessages) {
    }
}