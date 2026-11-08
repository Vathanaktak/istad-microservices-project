package kh.edu.istad.platform.iam;

import kh.edu.istad.platform.iam.domain.service.IamDomainService;
import kh.edu.istad.platform.iam.domain.service.IamDomainServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public IamDomainService iamDomainService() {
        return new IamDomainServiceImpl();
    }

}
