package io.github.trueruslan.zakupgotov.database;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class PostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18.4")
            .withDatabaseName("zakup_gotov")
            .withUsername("zakup_gotov")
            .withPassword("test-only-password");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    DSLContext dsl;

    @Test
    void migratesAppSchemaAndUsesPostgres18() {
        dsl.execute("set search_path to app");

        Record schemaRecord = dsl.fetchOne("select current_schema()");
        Record versionRecord = dsl.fetchOne("select current_setting('server_version_num')::int");

        assertThat(schemaRecord).isNotNull();
        assertThat(versionRecord).isNotNull();
        assertThat(schemaRecord.get(0, String.class)).isEqualTo("app");
        assertThat(versionRecord.get(0, Integer.class)).isGreaterThanOrEqualTo(180000);
    }
}
