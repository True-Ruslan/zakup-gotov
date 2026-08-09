package io.github.trueruslan.zakupgotov.database;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PostgresIntegrationTest extends PostgresIntegrationSupport {

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
