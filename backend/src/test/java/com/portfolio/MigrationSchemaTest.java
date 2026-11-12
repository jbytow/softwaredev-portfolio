package com.portfolio;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
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

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

    @Test
    @DisplayName("drops indexes that duplicate UNIQUE constraints or serve no query")
    void dropsRedundantIndexes() {
        List<String> present = jdbcTemplate.queryForList(
                "SELECT indexname FROM pg_indexes WHERE schemaname = 'public' AND indexname IN (?, ?, ?)",
                String.class, "idx_posts_slug", "idx_users_email", "idx_users_provider");

        assertThat(present).isEmpty();
    }

    @Test
    @DisplayName("keeps the UNIQUE indexes on posts.slug and users.email")
    void keepsUniqueIndexes() {
        List<String> present = jdbcTemplate.queryForList(
                "SELECT indexname FROM pg_indexes WHERE schemaname = 'public' AND indexname IN (?, ?)",
                String.class, "posts_slug_key", "users_email_key");

        assertThat(present).containsExactlyInAnyOrder("posts_slug_key", "users_email_key");
    }

    @Test
    @DisplayName("posts.published is NOT NULL, since the code unboxes it")
    void publishedFlagIsNotNull() {
        String nullable = jdbcTemplate.queryForObject(
                "SELECT is_nullable FROM information_schema.columns "
                        + "WHERE table_schema = 'public' AND table_name = 'posts' AND column_name = 'published'",
                String.class);

        assertThat(nullable).isEqualTo("NO");
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
