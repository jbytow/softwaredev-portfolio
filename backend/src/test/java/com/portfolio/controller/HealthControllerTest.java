package com.portfolio.controller;

import com.portfolio.config.TestSecurityConfig;
import com.portfolio.security.JwtService;
import com.portfolio.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.actuate.endpoint.IndicatedHealthDescriptor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = HealthController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration.class
        }
)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("HealthController")
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HealthEndpoint healthEndpoint;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("GET /api/health returns 200 and UP when Actuator reports UP")
    void upReturns200() throws Exception {
        endpointReports(Health.up().build());

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("Portfolio API"))
                .andExpect(jsonPath("$.timestamp").isNumber());
    }

    @Test
    @DisplayName("GET /api/health returns 503 and DOWN when Actuator reports DOWN")
    void downReturns503() throws Exception {
        endpointReports(Health.down().build());

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.service").value("Portfolio API"))
                .andExpect(jsonPath("$.timestamp").isNumber());
    }

    @Test
    @DisplayName("GET /api/health returns 503 and DOWN when Actuator reports OUT_OF_SERVICE")
    void outOfServiceReturns503() throws Exception {
        endpointReports(Health.outOfService().build());

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"));
    }

    @Test
    @DisplayName("GET /api/health returns 503 and DOWN when Actuator reports UNKNOWN")
    void unknownReturns503() throws Exception {
        endpointReports(Health.status(Status.UNKNOWN).build());

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"));
    }

    @Test
    @DisplayName("GET /api/health does not expose Actuator component details")
    void doesNotLeakDetails() throws Exception {
        endpointReports(
                Health.down().withDetail("db", "jdbc:postgresql://secret-host/portfolio").build());

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.details").doesNotExist())
                .andExpect(jsonPath("$.components").doesNotExist())
                .andExpect(jsonPath("$.db").doesNotExist());
    }

    // The endpoint's descriptor types have package-private constructors, so the test mirrors a Health into one
    private void endpointReports(Health health) {
        IndicatedHealthDescriptor descriptor = mock(IndicatedHealthDescriptor.class);
        given(descriptor.getStatus()).willReturn(health.getStatus());
        given(descriptor.getDetails()).willReturn(health.getDetails());
        given(healthEndpoint.health()).willReturn(descriptor);
    }
}
