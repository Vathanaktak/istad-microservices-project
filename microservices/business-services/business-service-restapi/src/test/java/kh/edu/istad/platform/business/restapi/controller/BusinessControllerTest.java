package kh.edu.istad.platform.business.restapi.controller;

import kh.edu.istad.common.exception.DomainException;
import kh.edu.istad.common.webflux.exception.DomainExceptionHandler;
import kh.edu.istad.platform.business.domain.usecase.GetBusinessByIdUseCase;
import kh.edu.istad.platform.business.domain.usecase.InitiateBusinessUseCase;
import kh.edu.istad.platform.business.domain.usecase.InitiateGetAllBusinessUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessControllerTest {

    @Mock
    private InitiateBusinessUseCase businessUseCase;
    @Mock
    private InitiateGetAllBusinessUseCase getAllBusinessUseCase;
    @Mock
    private GetBusinessByIdUseCase getBusinessByIdUseCase;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        BusinessController controller = new BusinessController(businessUseCase, getAllBusinessUseCase, getBusinessByIdUseCase);
        webTestClient = WebTestClient.bindToController(controller)
                .controllerAdvice(new DomainExceptionHandler())
                .build();
    }

    @Test
    void getBusinessById_whenNotFound_shouldReturn404AndErrorApiResponse() {
        UUID nonExistentId = UUID.randomUUID();
        when(getBusinessByIdUseCase.getBusinessById(nonExistentId))
                .thenReturn(Mono.error(new DomainException("Business not found with id: " + nonExistentId)));

        webTestClient.get()
                .uri("/api/businesses/{businessId}", nonExistentId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.statusCode").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Business not found with id: " + nonExistentId)
                .jsonPath("$.details").isEqualTo("Not Found");
    }
}
