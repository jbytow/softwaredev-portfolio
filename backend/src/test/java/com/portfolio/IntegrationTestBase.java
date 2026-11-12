package com.portfolio;

import com.portfolio.config.TestContainersConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests that require a full Spring context
 * and a PostgreSQL database via TestContainers.
 * Skipped without Docker so the default suite stays green on machines without it.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                // The OAuth2 client auto-configuration refuses an empty client id
                "spring.security.oauth2.client.registration.google.client-id=test-client-id",
                "spring.security.oauth2.client.registration.google.client-secret=test-client-secret",
                // Keep StorageService's startup directory inside the ignored build output
                "app.upload.path=target/test-uploads"
        })
@Import(TestContainersConfig.class)
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
public abstract class IntegrationTestBase {
}
