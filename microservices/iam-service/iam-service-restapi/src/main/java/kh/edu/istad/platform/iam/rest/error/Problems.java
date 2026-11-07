package kh.edu.istad.platform.iam.rest.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import org.springframework.http.*;

/** RFC 9457 helpers. */
public final class Problems {
    public static final String BASE = "https://iam.istad.co/problems/";
    private Problems() {}

    public static ProblemDetail of(HttpStatus status, String slug, String title, String detail) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, detail);
        p.setType(URI.create(BASE + slug));
        p.setTitle(title);
        return p;
    }

    public static void write(HttpServletResponse res, ObjectMapper om, HttpStatus status, String slug, String title, String detail) throws IOException {
        res.setStatus(status.value());
        res.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        om.writeValue(res.getOutputStream(), of(status, slug, title, detail));
    }
}
