package com.portfolio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.contributor.Status;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Boot 4 turns the Kubernetes liveness/readiness probes on by default, which adds health groups
 * and lets readiness flip the aggregate status; this app runs under Docker Compose, not Kubernetes.
 */
@DisplayName("Actuator health")
class ActuatorHealthTest extends IntegrationTestBase {

    @Autowired
    private HealthEndpoint healthEndpoint;

    @Test
    @DisplayName("reports UP for the running application")
    void reportsUp() {
        assertThat(healthEndpoint.health().getStatus()).isEqualTo(Status.UP);
    }

    @Test
    @DisplayName("exposes no liveness or readiness probe groups")
    void exposesNoProbeGroups() {
        assertThat(healthEndpoint.healthForPath("liveness")).isNull();
        assertThat(healthEndpoint.healthForPath("readiness")).isNull();
    }
}
