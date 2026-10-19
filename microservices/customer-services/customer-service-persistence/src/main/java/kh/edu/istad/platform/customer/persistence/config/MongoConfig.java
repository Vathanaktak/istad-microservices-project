package kh.edu.istad.platform.customer.persistence.config;

import kh.edu.istad.platform.customer.persistence.convert.BinaryToStringConverter;
import kh.edu.istad.platform.customer.persistence.convert.CustomerIdReadConverter;
import kh.edu.istad.platform.customer.persistence.convert.CustomerIdWriteConverter;
import kh.edu.istad.platform.customer.persistence.convert.EmailReadConverter;
import kh.edu.istad.platform.customer.persistence.convert.EmailWriteConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

@EnableReactiveMongoAuditing
@Configuration
public class MongoConfig {
    @Bean
    public MongoCustomConversions bean(){
        return MongoCustomConversions.create(
                adapter -> {
                    adapter.registerConverter(new BinaryToStringConverter());
                    adapter.registerConverter(new CustomerIdReadConverter());
                    adapter.registerConverter(new CustomerIdWriteConverter());
                    adapter.registerConverter(new EmailReadConverter());
                    adapter.registerConverter(new EmailWriteConverter());
                }
        );
    }
}
