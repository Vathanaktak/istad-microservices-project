package kh.edu.istad.platform.order.restapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "service")
@Getter
@Setter
public class ServicePropConfig {
    private String message;
    private String env;
    private String version;
    private String baseUrl; //base-url
}
