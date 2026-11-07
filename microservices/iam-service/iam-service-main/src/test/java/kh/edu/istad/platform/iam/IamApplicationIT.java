package kh.edu.istad.platform.iam;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class IamApplicationIT {
    @Container static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", pg::getJdbcUrl);
        r.add("spring.datasource.username", pg::getUsername);
        r.add("spring.datasource.password", pg::getPassword);
        r.add("iam.dcr.client-secret", () -> "test-secret");
    }

    @Autowired MockMvc mvc;

    private static final String REGISTER = """
        {"username":"sokdara","email":"dara.sok@istad.co","password":"Str0ng!Pass2026","confirmPassword":"Str0ng!Pass2026",
         "givenName":"Dara","familyName":"Sok","locale":"km"}""";

    @Test
    void register_is_public_and_returns_201() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.username").value("sokdara"))
            .andExpect(jsonPath("$.roles[0].name").value("USER"));
    }

    @Test
    void duplicate_registration_is_409_problem_json() throws Exception {
        String body = REGISTER.replace("sokdara", "dup1").replace("dara.sok@istad.co", "dup1@istad.co");
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isConflict())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void invalid_payload_is_400_with_errors() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"bad\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("https://iam.istad.co/problems/validation-error"))
            .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void protected_endpoint_requires_token() throws Exception {
        mvc.perform(get("/api/users")).andExpect(status().isUnauthorized());
    }

    @Test
    void permission_authority_is_enforced() throws Exception {
        mvc.perform(get("/api/roles").with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("user:read"))))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/roles").with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("role:read"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[?(@.name=='ADMIN')]").exists());
    }

    @Test
    void oidc_discovery_and_login_page_are_available() throws Exception {
        mvc.perform(get("/.well-known/openid-configuration")).andExpect(status().isOk())
            .andExpect(jsonPath("$.registration_endpoint").exists());
        mvc.perform(get("/login?lang=km")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("ចូលគណនី")));
    }
}
