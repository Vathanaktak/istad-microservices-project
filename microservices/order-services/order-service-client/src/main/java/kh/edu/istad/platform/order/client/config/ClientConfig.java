package kh.edu.istad.platform.order.client.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kh.edu.istad.platform.order.client.exception.DownStreamServiceException;
import kh.edu.istad.platform.order.client.httpClient.BusinessHttpClient;
import kh.edu.istad.platform.order.client.httpClient.CustomerHttpClient;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.support.WebClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.HttpServiceGroup;
import org.springframework.web.service.registry.ImportHttpServices;

import java.lang.reflect.Proxy;

@Configuration
@ImportHttpServices(group = "customer-service", types = CustomerHttpClient.class,
        clientType = HttpServiceGroup.ClientType.WEB_CLIENT)
@ImportHttpServices(group = "business-service", types = BusinessHttpClient.class,
        clientType = HttpServiceGroup.ClientType.WEB_CLIENT)
public class ClientConfig {

    private final HttpServletRequest dummyReq = (HttpServletRequest) Proxy.newProxyInstance(
            HttpServletRequest.class.getClassLoader(),
            new Class<?>[]{HttpServletRequest.class},
            (p, m, a) -> null);

    private final HttpServletResponse dummyRes = (HttpServletResponse) Proxy.newProxyInstance(
            HttpServletResponse.class.getClassLoader(),
            new Class<?>[]{HttpServletResponse.class},
            (p, m, a) -> null);

    @Bean
    WebClientHttpServiceGroupConfigurer lbAndErrors(ReactorLoadBalancerExchangeFilterFunction lb) {
        return groups -> groups.forEachClient((group, builder) -> builder
                .defaultRequest(spec -> {
                    RequestAttributes context = RequestContextHolder.getRequestAttributes();
                    HttpServletRequest req = null;
                    HttpServletResponse res = null;
                    if (context instanceof ServletRequestAttributes sra) {
                        req = sra.getRequest();
                        res = sra.getResponse();
                    }
                    spec.attribute(HttpServletRequest.class.getName(), req != null ? req : dummyReq);
                    spec.attribute(HttpServletResponse.class.getName(), res != null ? res : dummyRes);
                })
                .filter(lb)
                .defaultStatusHandler(HttpStatusCode::isError, res -> res.createException()
                        .map(ex -> new DownStreamServiceException(
                                group.name() + " returned " + ex.getStatusCode() + ": " + ex.getMessage(), group.name()))));
    }
}
