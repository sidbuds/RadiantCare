-- Report PDF export and consultation sharing upgrade.

SET @sql = IF(
  EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'exam_report' AND COLUMN_NAME = 'pdf_object_key'),
  'SELECT 1',
  'ALTER TABLE `exam_report` ADD COLUMN `pdf_object_key` VARCHAR(512) NULL AFTER `pdf_url`'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'doctor_consultation_reply' AND COLUMN_NAME = 'message_type'),
  'SELECT 1',
  'ALTER TABLE `doctor_consultation_reply` ADD COLUMN `message_type` VARCHAR(32) DEFAULT ''TEXT'' AFTER `attachment_url`'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'doctor_consultation_reply' AND COLUMN_NAME = 'ref_report_no'),
  'SELECT 1',
  'ALTER TABLE `doctor_consultation_reply` ADD COLUMN `ref_report_no` VARCHAR(32) NULL AFTER `message_type`'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
