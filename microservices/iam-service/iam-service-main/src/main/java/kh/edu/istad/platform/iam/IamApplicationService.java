package kh.edu.istad.platform.iam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Entry point. Component scanning covers every iam-* module (all live under kh.edu.istad.platform.iam). */
@SpringBootApplication
public class IamApplicationService {
    public static void main(String[] args) {
        SpringApplication.run(IamApplicationService.class, args);
    }
}
