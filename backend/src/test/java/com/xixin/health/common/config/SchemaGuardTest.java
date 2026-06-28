package com.xixin.health.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("SchemaGuard tests")
class SchemaGuardTest {

    @Test
    @DisplayName("missing columns are added once")
    void addsMissingColumns() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), (Object[]) org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(Integer.class)))
                .thenReturn(0);
        SchemaGuard guard = new SchemaGuard(jdbcTemplate);

        guard.run();

        verify(jdbcTemplate).execute("ALTER TABLE `exam_report` ADD COLUMN `pdf_object_key` VARCHAR(512) NULL AFTER `pdf_url`");
    }

    @Test
    @DisplayName("existing columns are not altered")
    void skipsExistingColumns() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), (Object[]) org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(Integer.class)))
                .thenReturn(1);
        SchemaGuard guard = new SchemaGuard(jdbcTemplate);

        guard.run();

        verify(jdbcTemplate, never()).execute(anyString());
    }
}
