package kh.edu.istad.platform.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

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
