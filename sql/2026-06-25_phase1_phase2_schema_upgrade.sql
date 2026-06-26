-- Phase 1/2 schema upgrade, MySQL 5.7+ compatible

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_apply_phase1_phase2_schema_upgrade$$
CREATE PROCEDURE sp_apply_phase1_phase2_schema_upgrade()
BEGIN
  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'staff_account' AND COLUMN_NAME = 'department_code'),
    'SELECT 1',
    'ALTER TABLE `staff_account` ADD COLUMN `department_code` VARCHAR(32) NULL AFTER `last_login_at`'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'staff_account' AND COLUMN_NAME = 'department_name'),
    'SELECT 1',
    'ALTER TABLE `staff_account` ADD COLUMN `department_name` VARCHAR(128) NULL AFTER `department_code`'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'staff_account' AND COLUMN_NAME = 'specialty'),
    'SELECT 1',
    'ALTER TABLE `staff_account` ADD COLUMN `specialty` VARCHAR(128) NULL AFTER `department_name`'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'staff_account' AND COLUMN_NAME = 'center_code'),
    'SELECT 1',
    'ALTER TABLE `staff_account` ADD COLUMN `center_code` VARCHAR(32) NULL AFTER `specialty`'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'doctor_department_rel'),
    'SELECT 1',
    'CREATE TABLE `doctor_department_rel` (
      `id` BIGINT NOT NULL AUTO_INCREMENT,
      `doctor_id` BIGINT NOT NULL,
      `department_code` VARCHAR(32) NOT NULL,
      `department_name` VARCHAR(128) NOT NULL,
      `center_code` VARCHAR(32) DEFAULT NULL,
      `is_primary` TINYINT NOT NULL DEFAULT 0,
      `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
      `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
      `created_by` BIGINT DEFAULT NULL,
      `updated_by` BIGINT DEFAULT NULL,
      `is_deleted` TINYINT NOT NULL DEFAULT 0,
      PRIMARY KEY (`id`),
      UNIQUE KEY `uk_doctor_department_rel` (`doctor_id`, `department_code`, `center_code`),
      KEY `idx_department_code` (`department_code`),
      KEY `idx_doctor_id` (`doctor_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'resource_capacity' AND COLUMN_NAME = 'department_code'),
    'SELECT 1',
    'ALTER TABLE `resource_capacity` ADD COLUMN `department_code` VARCHAR(32) NULL AFTER `resource_code`'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'resource_capacity' AND COLUMN_NAME = 'department_name'),
    'SELECT 1',
    'ALTER TABLE `resource_capacity` ADD COLUMN `department_name` VARCHAR(128) NULL AFTER `department_code`'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'doctor_consultation' AND COLUMN_NAME = 'department_code'),
    'SELECT 1',
    'ALTER TABLE `doctor_consultation` ADD COLUMN `department_code` VARCHAR(32) NULL AFTER `priority_level`'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'doctor_consultation' AND COLUMN_NAME = 'department_name'),
    'SELECT 1',
    'ALTER TABLE `doctor_consultation` ADD COLUMN `department_name` VARCHAR(128) NULL AFTER `department_code`'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'doctor_consultation' AND COLUMN_NAME = 'health_profile_shared'),
    'SELECT 1',
    'ALTER TABLE `doctor_consultation` ADD COLUMN `health_profile_shared` TINYINT NOT NULL DEFAULT 0 AFTER `department_name`'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'doctor_consultation_reply' AND COLUMN_NAME = 'message_type'),
    'SELECT 1',
    'ALTER TABLE `doctor_consultation_reply` ADD COLUMN `message_type` VARCHAR(32) NULL AFTER `attachment_url`'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'doctor_consultation_reply' AND COLUMN_NAME = 'ref_report_no'),
    'SELECT 1',
    'ALTER TABLE `doctor_consultation_reply` ADD COLUMN `ref_report_no` VARCHAR(32) NULL AFTER `message_type`'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'exam_result_attachment' AND COLUMN_NAME = 'task_item_id'),
    'SELECT 1',
    'ALTER TABLE `exam_result_attachment` ADD COLUMN `task_item_id` BIGINT NULL AFTER `result_id`'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'exam_result_attachment' AND COLUMN_NAME = 'upload_time'),
    'SELECT 1',
    'ALTER TABLE `exam_result_attachment` ADD COLUMN `upload_time` DATETIME(3) NULL AFTER `file_size`'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'exam_result_attachment' AND COLUMN_NAME = 'remark'),
    'SELECT 1',
    'ALTER TABLE `exam_result_attachment` ADD COLUMN `remark` VARCHAR(256) NULL AFTER `upload_time`'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'health_profile' AND COLUMN_NAME = 'medical_history'),
    'SELECT 1',
    'ALTER TABLE `health_profile` ADD COLUMN `medical_history` TEXT NULL AFTER `allergy_history`'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'health_profile' AND COLUMN_NAME = 'remark'),
    'SELECT 1',
    'ALTER TABLE `health_profile` ADD COLUMN `remark` VARCHAR(256) NULL AFTER `drinking_status`'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  UPDATE `health_profile`
  SET `medical_history` = `past_medical_history`
  WHERE (`medical_history` IS NULL OR `medical_history` = '')
    AND `past_medical_history` IS NOT NULL
    AND `past_medical_history` <> '';

  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'system_config'),
    'SELECT 1',
    'CREATE TABLE `system_config` (
      `id` BIGINT NOT NULL AUTO_INCREMENT,
      `config_key` VARCHAR(64) NOT NULL,
      `config_value` VARCHAR(1024) DEFAULT NULL,
      `data_type` VARCHAR(32) DEFAULT NULL,
      `config_group` VARCHAR(64) DEFAULT NULL,
      `remark` VARCHAR(256) DEFAULT NULL,
      `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
      `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
      `created_by` BIGINT DEFAULT NULL,
      `updated_by` BIGINT DEFAULT NULL,
      `is_deleted` TINYINT NOT NULL DEFAULT 0,
      PRIMARY KEY (`id`),
      UNIQUE KEY `uk_config_key` (`config_key`),
      KEY `idx_config_group` (`config_group`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
END$$

CALL sp_apply_phase1_phase2_schema_upgrade()$$
DROP PROCEDURE IF EXISTS sp_apply_phase1_phase2_schema_upgrade$$

DELIMITER ;
