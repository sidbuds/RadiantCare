package com.xixin.health.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SchemaGuard implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public SchemaGuard(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        ensureColumn("exam_report", "pdf_object_key",
                "ALTER TABLE `exam_report` ADD COLUMN `pdf_object_key` VARCHAR(512) NULL AFTER `pdf_url`");
        ensureColumn("doctor_consultation_reply", "message_type",
                "ALTER TABLE `doctor_consultation_reply` ADD COLUMN `message_type` VARCHAR(32) DEFAULT 'TEXT' AFTER `attachment_url`");
        ensureColumn("doctor_consultation_reply", "ref_report_no",
                "ALTER TABLE `doctor_consultation_reply` ADD COLUMN `ref_report_no` VARCHAR(32) NULL AFTER `message_type`");
        ensureColumn("operation_log", "module",
                "ALTER TABLE `operation_log` ADD COLUMN `module` VARCHAR(32) NULL AFTER `operator_type`");
        ensureColumn("operation_log", "action",
                "ALTER TABLE `operation_log` ADD COLUMN `action` VARCHAR(64) NULL AFTER `module`");
        ensureColumn("operation_log", "target_type",
                "ALTER TABLE `operation_log` ADD COLUMN `target_type` VARCHAR(32) NULL AFTER `action`");
        ensureColumn("operation_log", "target_id",
                "ALTER TABLE `operation_log` ADD COLUMN `target_id` VARCHAR(64) NULL AFTER `target_type`");
        ensureColumn("operation_log", "biz_type",
                "ALTER TABLE `operation_log` ADD COLUMN `biz_type` VARCHAR(32) NULL AFTER `id`");
        ensureColumn("operation_log", "biz_id",
                "ALTER TABLE `operation_log` ADD COLUMN `biz_id` VARCHAR(64) NULL AFTER `biz_type`");
        ensureColumn("operation_log", "before_data",
                "ALTER TABLE `operation_log` ADD COLUMN `before_data` TEXT NULL AFTER `biz_id`");
        ensureColumn("operation_log", "after_data",
                "ALTER TABLE `operation_log` ADD COLUMN `after_data` TEXT NULL AFTER `before_data`");
    }

    private void ensureColumn(String tableName, String columnName, String alterSql) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                new Object[]{tableName, columnName},
                Integer.class);
        if (count != null && count > 0) {
            return;
        }
        log.warn("数据库字段缺失，自动补齐: {}.{}", tableName, columnName);
        jdbcTemplate.execute(alterSql);
    }
}
