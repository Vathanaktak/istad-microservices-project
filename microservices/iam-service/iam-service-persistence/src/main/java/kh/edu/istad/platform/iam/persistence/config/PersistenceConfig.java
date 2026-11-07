package kh.edu.istad.platform.iam.persistence.config;

import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class PersistenceConfig {
    @Bean
    AuditorAware<String> auditorAware(CurrentActorProvider actor) {
        return () -> Optional.of(actor.currentActor());
    }
}
