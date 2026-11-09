package kh.edu.istad.platform.iam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Entry point. Component scanning covers every iam-* module (all live under kh.edu.istad.platform.iam). */
@SpringBootApplication
public class IamApplicationService {
    public static void main(String[] args) {
        loadDotenv();
        SpringApplication.run(IamApplicationService.class, args);
    }

    private static void loadDotenv() {
        List<Path> candidates = List.of(
            Path.of(".env"),
            Path.of("microservices/iam-service/.env"),
            Path.of("../.env")
        );
        for (Path path : candidates) {
            if (Files.exists(path)) {
                try {
                    List<String> lines = Files.readAllLines(path);
                    for (String line : lines) {
                        String trimmed = line.trim();
                        if (!trimmed.isEmpty() && !trimmed.startsWith("#") && trimmed.contains("=")) {
                            int idx = trimmed.indexOf('=');
                            String key = trimmed.substring(0, idx).trim();
                            String val = trimmed.substring(idx + 1).trim();
                            if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                                val = val.substring(1, val.length() - 1).trim();
                            }
                            if (!key.isEmpty() && !val.isBlank() && System.getProperty(key) == null && System.getenv(key) == null) {
                                System.setProperty(key, val);
                            }
                        }
                    }
                    System.out.println("[iam-service] Loaded environment variables from: " + path.toAbsolutePath());
                    break;
                } catch (IOException ignored) {
                }
            }
        }
    }
}
