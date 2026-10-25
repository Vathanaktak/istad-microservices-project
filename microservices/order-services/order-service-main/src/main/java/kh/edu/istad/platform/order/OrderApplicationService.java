package kh.edu.istad.platform.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class
OrderApplicationService {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplicationService.class, args);
    }

}
