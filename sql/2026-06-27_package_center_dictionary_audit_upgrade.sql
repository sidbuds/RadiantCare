-- Package center, dictionary, audit upgrade. MySQL 5.7+ compatible.

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_apply_20260627_package_dictionary_audit_upgrade$$
CREATE PROCEDURE sp_apply_20260627_package_dictionary_audit_upgrade()
BEGIN
  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'exam_package_center_rel'),
    'SELECT 1',
    'CREATE TABLE `exam_package_center_rel` (
      `id` BIGINT NOT NULL AUTO_INCREMENT,
      `package_id` BIGINT NOT NULL COMMENT ''套餐ID'',
      `center_code` VARCHAR(32) NOT NULL COMMENT ''体检中心编码'',
      `center_name` VARCHAR(128) DEFAULT NULL COMMENT ''体检中心名称'',
      `status` TINYINT NOT NULL DEFAULT 1 COMMENT ''状态：1启用 0停用'',
      `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
      `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
      `created_by` BIGINT DEFAULT NULL,
      `updated_by` BIGINT DEFAULT NULL,
      `is_deleted` TINYINT NOT NULL DEFAULT 0,
      PRIMARY KEY (`id`),
      UNIQUE KEY `uk_package_center` (`package_id`,`center_code`,`is_deleted`),
      KEY `idx_center_code` (`center_code`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT=''套餐适用体检中心关系表'''
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  UPDATE `doctor_department_rel` SET `center_code` = 'DEFAULT' WHERE `center_code` IS NULL OR `center_code` = '';
  ALTER TABLE `doctor_department_rel` MODIFY COLUMN `center_code` VARCHAR(32) NOT NULL COMMENT '中心编码';

  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'operation_log' AND COLUMN_NAME = 'biz_type'),
    'SELECT 1',
    'ALTER TABLE `operation_log` ADD COLUMN `biz_type` VARCHAR(32) NULL COMMENT ''业务类型'' AFTER `id`'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'operation_log' AND COLUMN_NAME = 'biz_id'),
    'SELECT 1',
    'ALTER TABLE `operation_log` ADD COLUMN `biz_id` VARCHAR(64) NULL COMMENT ''业务ID'' AFTER `biz_type`'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'operation_log' AND COLUMN_NAME = 'operator_name'),
    'SELECT 1',
    'ALTER TABLE `operation_log` ADD COLUMN `operator_name` VARCHAR(64) NULL COMMENT ''操作人姓名'' AFTER `operator_id`'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'operation_log' AND COLUMN_NAME = 'operator_type'),
    'SELECT 1',
    'ALTER TABLE `operation_log` ADD COLUMN `operator_type` VARCHAR(32) NULL COMMENT ''操作人类型'' AFTER `operator_name`'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'operation_log' AND COLUMN_NAME = 'module'),
    'SELECT 1',
    'ALTER TABLE `operation_log` ADD COLUMN `module` VARCHAR(32) NULL COMMENT ''模块'' AFTER `operator_type`'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'operation_log' AND COLUMN_NAME = 'target_type'),
    'SELECT 1',
    'ALTER TABLE `operation_log` ADD COLUMN `target_type` VARCHAR(32) NULL COMMENT ''目标类型'' AFTER `action`'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'operation_log' AND COLUMN_NAME = 'target_id'),
    'SELECT 1',
    'ALTER TABLE `operation_log` ADD COLUMN `target_id` VARCHAR(64) NULL COMMENT ''目标ID'' AFTER `target_type`'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'operation_log' AND INDEX_NAME = 'idx_operation_module_action'),
    'SELECT 1',
    'ALTER TABLE `operation_log` ADD INDEX `idx_operation_module_action` (`module`,`action`)'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'operation_log' AND INDEX_NAME = 'idx_operation_target'),
    'SELECT 1',
    'ALTER TABLE `operation_log` ADD INDEX `idx_operation_target` (`target_type`,`target_id`)'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
END$$

CALL sp_apply_20260627_package_dictionary_audit_upgrade()$$
DROP PROCEDURE IF EXISTS sp_apply_20260627_package_dictionary_audit_upgrade$$

DELIMITER ;

INSERT INTO `data_dictionary` (`dict_type`, `dict_code`, `dict_name`, `sort_no`, `status`, `remark`, `is_deleted`)
VALUES
('EXAM_ITEM','BLOOD_ROUTINE','血常规',10,1,'检验科常规项目',0),
('EXAM_ITEM','URINE_ROUTINE','尿常规',20,1,'检验科常规项目',0),
('EXAM_ITEM','LIVER_FUNCTION','肝功能',30,1,'检验科生化项目',0),
('EXAM_ITEM','KIDNEY_FUNCTION','肾功能',40,1,'检验科生化项目',0),
('EXAM_ITEM','BLOOD_GLUCOSE','空腹血糖',50,1,'需空腹',0),
('EXAM_ITEM','BLOOD_LIPID','血脂四项',60,1,'需空腹',0),
('EXAM_ITEM','ECG','心电图',70,1,'心电图室',0),
('EXAM_ITEM','CHEST_DR','胸部正位片',80,1,'放射科',0),
('EXAM_ITEM','ABDOMEN_ULTRASOUND','腹部彩超',90,1,'需空腹',0),
('EXAM_ITEM','INTERNAL_MEDICINE','内科检查',100,1,'内科',0),
('EXAM_ITEM','SURGERY','外科检查',110,1,'外科',0),
('TIME_SLOT','AM_0800_0830','08:00-08:30',10,1,NULL,0),
('TIME_SLOT','AM_0830_0900','08:30-09:00',20,1,NULL,0),
('TIME_SLOT','AM_0900_0930','09:00-09:30',30,1,NULL,0),
('TIME_SLOT','AM_0930_1000','09:30-10:00',40,1,NULL,0),
('TIME_SLOT','AM_1000_1030','10:00-10:30',50,1,NULL,0),
('TIME_SLOT','AM_1030_1100','10:30-11:00',60,1,NULL,0),
('TIME_SLOT','PM_1400_1430','14:00-14:30',70,1,NULL,0),
('TIME_SLOT','PM_1430_1500','14:30-15:00',80,1,NULL,0),
('EXAM_DEPARTMENT','LAB','检验科',10,1,NULL,0),
('EXAM_DEPARTMENT','ULTRASOUND','超声科',20,1,NULL,0),
('EXAM_DEPARTMENT','RADIOLOGY','放射科',30,1,NULL,0),
('EXAM_DEPARTMENT','ECG_ROOM','心电图室',40,1,NULL,0),
('EXAM_DEPARTMENT','INTERNAL','内科',50,1,NULL,0),
('EXAM_DEPARTMENT','SURGERY_DEPT','外科',60,1,NULL,0),
('PACKAGE_CATEGORY','BASIC','基础体检',10,1,NULL,0),
('PACKAGE_CATEGORY','STANDARD','标准体检',20,1,NULL,0),
('PACKAGE_CATEGORY','PREMIUM','高端体检',30,1,NULL,0),
('PACKAGE_CATEGORY','CORPORATE','企业体检',40,1,NULL,0),
('RESOURCE_TYPE','CENTER_SLOT','中心总容量',10,1,NULL,0),
('AUDIT_MODULE','ROLE','角色管理',10,1,NULL,0),
('AUDIT_MODULE','DOCTOR','医生管理',20,1,NULL,0),
('AUDIT_MODULE','PACKAGE','套餐管理',30,1,NULL,0),
('AUDIT_MODULE','ROUTE','导引路线',40,1,NULL,0),
('AUDIT_MODULE','SCHEDULE','排班管理',50,1,NULL,0),
('AUDIT_MODULE','DICT','字典管理',60,1,NULL,0),
('AUDIT_MODULE','CONFIG','系统配置',70,1,NULL,0),
('AUDIT_ACTION','CREATE','新增',10,1,NULL,0),
('AUDIT_ACTION','UPDATE','编辑',20,1,NULL,0),
('AUDIT_ACTION','DELETE','删除',30,1,NULL,0),
('AUDIT_ACTION','ENABLE','启用',40,1,NULL,0),
('AUDIT_ACTION','DISABLE','停用',50,1,NULL,0),
('AUDIT_ACTION','BIND','绑定',60,1,NULL,0),
('AUDIT_ACTION','UNBIND','解绑',70,1,NULL,0)
ON DUPLICATE KEY UPDATE
  `dict_name` = VALUES(`dict_name`),
  `sort_no` = VALUES(`sort_no`),
  `status` = VALUES(`status`),
  `remark` = VALUES(`remark`),
  `is_deleted` = 0;

INSERT INTO `system_config` (`config_key`, `config_value`, `data_type`, `config_group`, `remark`, `is_deleted`)
VALUES
('appointment.advance_days','30','INT','APPOINTMENT','允许提前预约天数',0),
('appointment.allow_today','false','BOOLEAN','APPOINTMENT','是否允许当天预约',0),
('schedule.default_capacity','20','INT','SCHEDULE','中心时段默认容量',0)
ON DUPLICATE KEY UPDATE
  `config_value` = VALUES(`config_value`),
  `data_type` = VALUES(`data_type`),
  `config_group` = VALUES(`config_group`),
  `remark` = VALUES(`remark`),
  `is_deleted` = 0;
