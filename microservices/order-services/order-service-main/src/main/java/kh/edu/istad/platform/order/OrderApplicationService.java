package kh.edu.istad.platform.order;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties
public class
OrderApplicationService {

//    private final DiscoveryClient client;

    public static void main(String[] args) {
        SpringApplication.run(OrderApplicationService.class, args);
    }



//    @Bean
//    public CommandLineRunner cmd(){
//        return _->{
//            client.getInstances(
//                    "customer-service"
//            ).forEach(
//                    instance -> System.out.println(instance.getUri())
//            );
//        };
//    }

}
