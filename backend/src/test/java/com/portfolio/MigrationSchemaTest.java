package com.portfolio;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the real Flyway migrations against PostgreSQL and lets Hibernate validate
 * the entity mappings against the result, the same way production starts.
 */
@DisplayName("Flyway migrations and entity mappings")
class MigrationSchemaTest extends IntegrationTestBase {

    private static final Pattern MIGRATION_VERSION = Pattern.compile("^V(\\d+)__.+\\.sql$");

    @Autowired
    private Flyway flyway;

    @Test
    @DisplayName("application context starts with ddl-auto=validate on the migrated schema")
    void contextLoads() {
        // Startup itself is the assertion: a mapping/schema mismatch fails context creation.
    }

    @Test
    @DisplayName("applies every migration up to the highest V*.sql on the classpath")
    void appliesAllMigrations() throws IOException {
        assertThat(flyway.info().current().getVersion().getVersion())
                .isEqualTo(String.valueOf(highestMigrationVersion()));
        assertThat(flyway.info().pending()).isEmpty();
    }

    private static int highestMigrationVersion() throws IOException {
        Resource[] scripts = new PathMatchingResourcePatternResolver()
                .getResources("classpath:db/migration/V*__*.sql");
        return Arrays.stream(scripts)
                .map(Resource::getFilename)
                .map(MIGRATION_VERSION::matcher)
                .filter(Matcher::matches)
                .mapToInt(m -> Integer.parseInt(m.group(1)))
                .max()
                .orElseThrow();
    }
}
