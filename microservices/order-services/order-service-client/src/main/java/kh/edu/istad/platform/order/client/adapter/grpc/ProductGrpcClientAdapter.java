package kh.edu.istad.platform.order.client.adapter.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import kh.edu.istad.platform.order.domain.dto.client.QueryProductResult;
import kh.edu.istad.platform.order.domain.port.out.client.ProductServiceClient;
import kh.edu.istad.product.grpc.ProductGrpcServiceGrpc;
import kh.edu.istad.product.grpc.ProductRequest;
import kh.edu.istad.product.grpc.ProductResponse;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@Slf4j
public class ProductGrpcClientAdapter implements ProductServiceClient {

    @GrpcClient("product-service")
    private ProductGrpcServiceGrpc.ProductGrpcServiceBlockingStub productBlockingStub;

    @Override
    public Mono<QueryProductResult> getProductById(String productId) {
        return Mono.fromCallable(() -> {
                    ProductRequest request = ProductRequest.newBuilder()
                            .setProductId(productId)
                            .build();
                    ProductResponse response = productBlockingStub.getProductById(request);
                    return new QueryProductResult(
                            response.getProductId(),
                            response.getName(),
                            response.getPrice(),
                            response.getQuantity()
                    );
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(StatusRuntimeException.class, this::translateGrpcException);
    }

    // Exception Translator from gRPC Status -> HTTP Status
    private Throwable translateGrpcException(StatusRuntimeException ex) {
        Status.Code code = ex.getStatus().getCode();
        String description = ex.getStatus().getDescription();
        log.warn("gRPC Call failed with status [{}]: {}", code, description);

        return switch (code) {
            case NOT_FOUND ->
                    new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            description != null ? description : "Product not found"
                    );
            case INVALID_ARGUMENT ->
                    new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            description != null ? description : "Invalid product argument"
                    );
            case UNAVAILABLE ->
                    new ResponseStatusException(
                            HttpStatus.SERVICE_UNAVAILABLE, "Product service is currently unavailable"
                    );
            case DEADLINE_EXCEEDED ->
                    new ResponseStatusException(
                            HttpStatus.GATEWAY_TIMEOUT, "Product service call timed out"
                    );
            default ->
                    new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR, "Internal gRPC communication error: " + description
                    );
        };
    }
}
