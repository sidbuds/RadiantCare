DROP DATABASE IF EXISTS `xixin_health`;
CREATE DATABASE `xixin_health` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `xixin_health`;

-- ============ 用户 & 认证 ============

CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_no` VARCHAR(32) NOT NULL,
  `name` VARCHAR(64) NOT NULL,
  `gender` TINYINT NOT NULL DEFAULT 0,
  `birth_date` DATE DEFAULT NULL,
  `id_type` TINYINT DEFAULT NULL,
  `id_no` VARCHAR(64) DEFAULT NULL,
  `mobile` VARCHAR(20) NOT NULL,
  `email` VARCHAR(128) DEFAULT NULL,
  `password_hash` VARCHAR(128) DEFAULT NULL,
  `address` VARCHAR(256) DEFAULT NULL,
  `emergency_contact` VARCHAR(64) DEFAULT NULL,
  `emergency_mobile` VARCHAR(20) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `last_login_at` DATETIME(3) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_no` (`user_no`),
  KEY `idx_mobile` (`mobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `staff_account` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(64) NOT NULL,
  `password_hash` VARCHAR(128) NOT NULL,
  `display_name` VARCHAR(64) DEFAULT NULL,
  `bind_user_id` BIGINT DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `last_login_at` DATETIME(3) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `staff_role_rel` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `staff_account_id` BIGINT NOT NULL,
  `role_code` VARCHAR(32) NOT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_staff_account` (`staff_account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `health_profile` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `allergy_history` TEXT,
  `past_medical_history` TEXT,
  `family_history` TEXT,
  `medication_history` TEXT,
  `smoking_status` TINYINT DEFAULT 0,
  `drinking_status` TINYINT DEFAULT 0,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============ 套餐 & 中心 ============

CREATE TABLE `exam_package` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `package_code` VARCHAR(32) NOT NULL,
  `package_name` VARCHAR(128) NOT NULL,
  `category` VARCHAR(32) DEFAULT NULL,
  `price` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `status` TINYINT NOT NULL DEFAULT 1,
  `remark` VARCHAR(512) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_package_code` (`package_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `exam_package_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `package_id` BIGINT NOT NULL,
  `item_code` VARCHAR(32) NOT NULL,
  `item_name` VARCHAR(128) NOT NULL,
  `unit` VARCHAR(32) DEFAULT NULL,
  `ref_range` VARCHAR(128) DEFAULT NULL,
  `sort_no` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_package_item` (`package_id`,`item_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `exam_center` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `center_code` VARCHAR(32) NOT NULL,
  `center_name` VARCHAR(128) NOT NULL,
  `address` VARCHAR(256) DEFAULT NULL,
  `phone` VARCHAR(32) DEFAULT NULL,
  `business_hours` VARCHAR(128) DEFAULT NULL,
  `description` TEXT DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_center_code` (`center_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============ 预约 & 资源 ============

CREATE TABLE `appointment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `appointment_no` VARCHAR(32) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `package_id` BIGINT NOT NULL,
  `center_code` VARCHAR(32) NOT NULL,
  `appoint_date` DATE NOT NULL,
  `time_slot_code` VARCHAR(32) NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 0,
  `cancel_reason` VARCHAR(256) DEFAULT NULL,
  `remark` VARCHAR(256) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_appointment_no` (`appointment_no`),
  KEY `idx_user_date` (`user_id`,`appoint_date`),
  KEY `idx_center_date_slot` (`center_code`,`appoint_date`,`time_slot_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `appointment_queue` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `queue_no` VARCHAR(32) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `package_id` BIGINT NOT NULL,
  `center_code` VARCHAR(32) NOT NULL,
  `appoint_date` DATE NOT NULL,
  `time_slot_code` VARCHAR(32) NOT NULL,
  `priority` INT NOT NULL DEFAULT 0,
  `queue_status` TINYINT NOT NULL DEFAULT 0,
  `expire_at` DATETIME(3) DEFAULT NULL,
  `confirmed_at` DATETIME(3) DEFAULT NULL,
  `remark` VARCHAR(256) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_queue_no` (`queue_no`),
  KEY `idx_queue_user_date` (`user_id`,`appoint_date`),
  KEY `idx_queue_center_slot` (`center_code`,`appoint_date`,`time_slot_code`,`queue_status`,`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `resource_capacity` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `center_code` VARCHAR(32) NOT NULL,
  `appoint_date` DATE NOT NULL,
  `time_slot_code` VARCHAR(32) NOT NULL,
  `resource_type` VARCHAR(32) NOT NULL,
  `resource_code` VARCHAR(64) NOT NULL,
  `capacity_total` INT NOT NULL DEFAULT 0,
  `capacity_used` INT NOT NULL DEFAULT 0,
  `capacity_locked` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `version_no` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resource_capacity` (`center_code`,`appoint_date`,`time_slot_code`,`resource_type`,`resource_code`),
  KEY `idx_capacity_slot` (`center_code`,`appoint_date`,`time_slot_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `resource_snapshot` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `snapshot_no` VARCHAR(32) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `center_code` VARCHAR(32) NOT NULL,
  `appoint_date` DATE NOT NULL,
  `time_slot_code` VARCHAR(32) NOT NULL,
  `resource_type` VARCHAR(32) NOT NULL,
  `resource_code` VARCHAR(64) NOT NULL,
  `lock_status` TINYINT NOT NULL DEFAULT 0,
  `expire_at` DATETIME(3) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_snapshot_no` (`snapshot_no`),
  KEY `idx_snapshot_slot` (`center_code`,`appoint_date`,`time_slot_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `schedule_resource_bind` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `center_code` VARCHAR(32) NOT NULL,
  `time_slot_code` VARCHAR(32) NOT NULL,
  `package_id` BIGINT DEFAULT NULL,
  `item_code` VARCHAR(32) DEFAULT NULL,
  `resource_type` VARCHAR(32) NOT NULL,
  `resource_code` VARCHAR(64) NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_schedule_resource_bind` (`center_code`,`time_slot_code`,`package_id`,`item_code`,`resource_type`,`resource_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============ 订单 & 支付 & 退款 ============

CREATE TABLE `order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(32) NOT NULL,
  `appointment_id` BIGINT DEFAULT NULL,
  `user_id` BIGINT NOT NULL,
  `package_id` BIGINT DEFAULT NULL,
  `total_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `pay_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `discount_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `status` VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  `pay_channel` VARCHAR(32) DEFAULT NULL,
  `pay_time` DATETIME(3) DEFAULT NULL,
  `remark` VARCHAR(256) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_appointment_id` (`appointment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `item_type` VARCHAR(32) DEFAULT NULL,
  `ref_item_code` VARCHAR(32) DEFAULT NULL,
  `ref_item_name` VARCHAR(128) DEFAULT NULL,
  `qty` INT NOT NULL DEFAULT 1,
  `unit_price` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `order_snapshot` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(32) NOT NULL,
  `snapshot_type` VARCHAR(32) NOT NULL,
  `snapshot_data` JSON DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `order_settlement_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(32) NOT NULL,
  `settle_type` VARCHAR(32) NOT NULL,
  `settle_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `settle_status` TINYINT NOT NULL DEFAULT 0,
  `settle_time` DATETIME(3) DEFAULT NULL,
  `remark` VARCHAR(256) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `payment_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `payment_no` VARCHAR(32) NOT NULL,
  `order_no` VARCHAR(32) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `pay_method` VARCHAR(32) NOT NULL,
  `pay_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `pay_status` TINYINT NOT NULL DEFAULT 0,
  `pay_time` DATETIME(3) DEFAULT NULL,
  `third_party_no` VARCHAR(64) DEFAULT NULL,
  `remark` VARCHAR(256) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `refund_apply` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `apply_no` VARCHAR(32) NOT NULL,
  `order_id` BIGINT NOT NULL,
  `order_no` VARCHAR(32) NOT NULL,
  `refund_type` VARCHAR(32) DEFAULT NULL,
  `apply_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `reason` VARCHAR(512) DEFAULT NULL,
  `apply_status` TINYINT NOT NULL DEFAULT 0,
  `audit_status` TINYINT DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_apply_no` (`apply_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `refund_audit_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `apply_no` VARCHAR(32) NOT NULL,
  `auditor_id` BIGINT NOT NULL,
  `audit_action` VARCHAR(32) NOT NULL,
  `audit_remark` VARCHAR(512) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_apply_no` (`apply_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `refund_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `refund_no` VARCHAR(32) NOT NULL,
  `apply_no` VARCHAR(32) NOT NULL,
  `order_no` VARCHAR(32) NOT NULL,
  `refund_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `refund_status` TINYINT NOT NULL DEFAULT 0,
  `refund_time` DATETIME(3) DEFAULT NULL,
  `third_party_no` VARCHAR(64) DEFAULT NULL,
  `remark` VARCHAR(256) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_apply_no` (`apply_no`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============ 体检任务 & 结果 ============

CREATE TABLE `exam_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `task_no` VARCHAR(32) NOT NULL,
  `appointment_id` BIGINT DEFAULT NULL,
  `appointment_no` VARCHAR(32) DEFAULT NULL,
  `order_id` BIGINT DEFAULT NULL,
  `order_no` VARCHAR(32) DEFAULT NULL,
  `user_id` BIGINT NOT NULL,
  `package_id` BIGINT DEFAULT NULL,
  `center_code` VARCHAR(32) NOT NULL,
  `task_date` DATE NOT NULL,
  `task_status` TINYINT NOT NULL DEFAULT 0,
  `report_status` VARCHAR(32) DEFAULT NULL,
  `guide_status` TINYINT DEFAULT 0,
  `arrive_time` DATETIME(3) DEFAULT NULL,
  `start_time` DATETIME(3) DEFAULT NULL,
  `complete_time` DATETIME(3) DEFAULT NULL,
  `remark` VARCHAR(256) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_no` (`task_no`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_center_date` (`center_code`,`task_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `exam_task_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `task_item_no` VARCHAR(32) NOT NULL,
  `task_id` BIGINT DEFAULT NULL,
  `task_no` VARCHAR(32) NOT NULL,
  `item_code` VARCHAR(32) NOT NULL,
  `item_name` VARCHAR(128) DEFAULT NULL,
  `department_code` VARCHAR(32) DEFAULT NULL,
  `department_name` VARCHAR(64) DEFAULT NULL,
  `doctor_id` BIGINT DEFAULT NULL,
  `doctor_name` VARCHAR(64) DEFAULT NULL,
  `room_no` VARCHAR(32) DEFAULT NULL,
  `route_sort` INT DEFAULT 0,
  `item_status` TINYINT NOT NULL DEFAULT 0,
  `entry_status` TINYINT DEFAULT 0,
  `start_time` DATETIME(3) DEFAULT NULL,
  `complete_time` DATETIME(3) DEFAULT NULL,
  `skip_reason` VARCHAR(256) DEFAULT NULL,
  `remark` VARCHAR(256) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_item_no` (`task_item_no`),
  KEY `idx_task_no` (`task_no`),
  KEY `idx_doctor_id` (`doctor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `exam_department_route` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `route_code` VARCHAR(32) NOT NULL,
  `center_code` VARCHAR(32) NOT NULL,
  `package_id` BIGINT NOT NULL,
  `item_code` VARCHAR(32) NOT NULL,
  `department_code` VARCHAR(32) NOT NULL,
  `department_name` VARCHAR(64) DEFAULT NULL,
  `room_no` VARCHAR(32) DEFAULT NULL,
  `floor_no` VARCHAR(16) DEFAULT NULL,
  `building_no` VARCHAR(16) DEFAULT NULL,
  `route_sort` INT NOT NULL DEFAULT 0,
  `guide_text` VARCHAR(512) DEFAULT NULL,
  `need_empty_stomach` TINYINT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_route_package_item` (`center_code`,`package_id`,`item_code`),
  KEY `idx_route_center_package` (`center_code`,`package_id`,`route_sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `exam_result` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `result_no` VARCHAR(32) DEFAULT NULL,
  `task_id` BIGINT DEFAULT NULL,
  `task_item_id` BIGINT DEFAULT NULL,
  `task_item_no` VARCHAR(32) NOT NULL,
  `task_no` VARCHAR(32) DEFAULT NULL,
  `user_id` BIGINT DEFAULT NULL,
  `report_id` BIGINT DEFAULT NULL,
  `item_code` VARCHAR(32) NOT NULL,
  `item_name` VARCHAR(128) DEFAULT NULL,
  `result_type` VARCHAR(32) DEFAULT NULL,
  `result_value` VARCHAR(256) DEFAULT NULL,
  `result_number` DECIMAL(16,4) DEFAULT NULL,
  `unit` VARCHAR(32) DEFAULT NULL,
  `ref_range` VARCHAR(128) DEFAULT NULL,
  `conclusion` VARCHAR(512) DEFAULT NULL,
  `is_abnormal` TINYINT NOT NULL DEFAULT 0,
  `abnormal_level` TINYINT NOT NULL DEFAULT 0,
  `entry_doctor_id` BIGINT DEFAULT NULL,
  `entry_doctor_name` VARCHAR(64) DEFAULT NULL,
  `entry_time` DATETIME(3) DEFAULT NULL,
  `audit_status` TINYINT DEFAULT 0,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_task_item` (`task_no`,`task_item_no`),
  KEY `idx_entry_doctor` (`entry_doctor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `exam_result_attachment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `result_id` BIGINT NOT NULL,
  `file_name` VARCHAR(128) NOT NULL,
  `file_url` VARCHAR(512) NOT NULL,
  `file_type` VARCHAR(32) DEFAULT NULL,
  `file_size` BIGINT DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_result_id` (`result_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============ 报告 ============

CREATE TABLE `report_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `template_code` VARCHAR(64) NOT NULL,
  `template_name` VARCHAR(128) NOT NULL,
  `package_id` BIGINT DEFAULT NULL,
  `template_type` VARCHAR(32) DEFAULT NULL,
  `version_no` INT NOT NULL DEFAULT 1,
  `render_engine` VARCHAR(32) DEFAULT NULL,
  `template_config` JSON DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_code` (`template_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `report_section_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `template_id` BIGINT NOT NULL,
  `section_code` VARCHAR(32) NOT NULL,
  `section_name` VARCHAR(64) NOT NULL,
  `section_type` VARCHAR(32) DEFAULT NULL,
  `data_source_type` VARCHAR(32) DEFAULT NULL,
  `item_codes` VARCHAR(512) DEFAULT NULL,
  `sort_no` INT NOT NULL DEFAULT 0,
  `render_rule` JSON DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `exam_report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `report_no` VARCHAR(32) NOT NULL,
  `appointment_id` BIGINT DEFAULT NULL,
  `task_id` BIGINT DEFAULT NULL,
  `user_id` BIGINT NOT NULL,
  `package_id` BIGINT DEFAULT NULL,
  `template_id` BIGINT DEFAULT NULL,
  `report_date` DATE DEFAULT NULL,
  `overall_conclusion` TEXT,
  `status` VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  `pdf_url` VARCHAR(512) DEFAULT NULL,
  `version_no` INT DEFAULT 1,
  `published_at` DATETIME(3) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_report_no` (`report_no`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `exam_report_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `report_id` BIGINT NOT NULL,
  `item_code` VARCHAR(32) NOT NULL,
  `item_name` VARCHAR(128) DEFAULT NULL,
  `result_value` VARCHAR(256) DEFAULT NULL,
  `result_number` DECIMAL(16,4) DEFAULT NULL,
  `unit` VARCHAR(32) DEFAULT NULL,
  `ref_range` VARCHAR(128) DEFAULT NULL,
  `is_abnormal` TINYINT NOT NULL DEFAULT 0,
  `abnormal_level` TINYINT NOT NULL DEFAULT 0,
  `sort_no` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_report_id` (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `exam_report_item_abnormal` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `report_item_id` BIGINT NOT NULL,
  `report_no` VARCHAR(32) NOT NULL,
  `metric_code` VARCHAR(32) NOT NULL,
  `abnormal_level` TINYINT NOT NULL DEFAULT 0,
  `abnormal_desc` VARCHAR(512) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_report_item` (`report_item_id`),
  KEY `idx_report_no` (`report_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `doctor_review_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `review_no` VARCHAR(32) DEFAULT NULL,
  `report_id` BIGINT NOT NULL,
  `task_id` BIGINT DEFAULT NULL,
  `review_stage` VARCHAR(32) NOT NULL,
  `review_status` VARCHAR(32) NOT NULL,
  `review_comment` TEXT,
  `reviewer_id` BIGINT NOT NULL,
  `reviewer_name` VARCHAR(64) DEFAULT NULL,
  `reviewed_at` DATETIME(3) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_report_id` (`report_id`),
  KEY `idx_reviewer_id` (`reviewer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============ 对比 & 健康建议 ============

CREATE TABLE `report_compare_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `task_no` VARCHAR(32) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `baseline_report_id` BIGINT NOT NULL,
  `compare_report_id` BIGINT NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_no` (`task_no`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `report_compare_result` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `task_id` BIGINT NOT NULL,
  `item_code` VARCHAR(32) NOT NULL,
  `item_name` VARCHAR(128) DEFAULT NULL,
  `base_value` VARCHAR(256) DEFAULT NULL,
  `compare_value` VARCHAR(256) DEFAULT NULL,
  `change_value` VARCHAR(256) DEFAULT NULL,
  `change_rate` DECIMAL(8,2) DEFAULT NULL,
  `trend` VARCHAR(16) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `report_indicator_yearly` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `metric_code` VARCHAR(32) NOT NULL,
  `metric_name` VARCHAR(128) DEFAULT NULL,
  `year_value` INT NOT NULL,
  `result_value` VARCHAR(256) DEFAULT NULL,
  `result_number` DECIMAL(16,4) DEFAULT NULL,
  `unit` VARCHAR(32) DEFAULT NULL,
  `report_no` VARCHAR(32) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_metric_year` (`user_id`,`metric_code`,`year_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `indicator_trend_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `task_id` BIGINT DEFAULT NULL,
  `item_code` VARCHAR(32) NOT NULL,
  `trend_tag` VARCHAR(32) NOT NULL,
  `trend_direction` VARCHAR(16) DEFAULT NULL,
  `turning_year` INT DEFAULT NULL,
  `risk_level` TINYINT DEFAULT 0,
  `tag_reason` VARCHAR(256) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_task_item` (`task_id`,`item_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `health_risk_score` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `task_id` BIGINT DEFAULT NULL,
  `user_id` BIGINT NOT NULL,
  `score_total` DECIMAL(5,2) NOT NULL DEFAULT 0.00,
  `score_abnormal` DECIMAL(5,2) DEFAULT 0.00,
  `score_trend` DECIMAL(5,2) DEFAULT 0.00,
  `risk_level` TINYINT NOT NULL DEFAULT 0,
  `score_detail` JSON DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_task` (`user_id`,`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `health_advice_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `advice_no` VARCHAR(32) DEFAULT NULL,
  `user_id` BIGINT NOT NULL,
  `report_id` BIGINT DEFAULT NULL,
  `compare_task_id` BIGINT DEFAULT NULL,
  `source_type` VARCHAR(32) DEFAULT NULL,
  `advice_type` VARCHAR(32) NOT NULL,
  `risk_level` TINYINT DEFAULT 0,
  `advice_title` VARCHAR(128) DEFAULT NULL,
  `advice_content` TEXT,
  `action_suggestion` TEXT,
  `status` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_compare` (`user_id`,`compare_task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============ 咨询 ============

CREATE TABLE `doctor_consultation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `consultation_no` VARCHAR(32) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `doctor_id` BIGINT DEFAULT NULL,
  `doctor_name` VARCHAR(64) DEFAULT NULL,
  `report_id` BIGINT DEFAULT NULL,
  `report_no` VARCHAR(32) DEFAULT NULL,
  `source_type` VARCHAR(32) DEFAULT NULL,
  `consultation_type` VARCHAR(32) DEFAULT NULL,
  `consultation_title` VARCHAR(128) DEFAULT NULL,
  `consultation_content` TEXT,
  `consultation_status` VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  `priority_level` TINYINT NOT NULL DEFAULT 0,
  `latest_reply_time` DATETIME(3) DEFAULT NULL,
  `closed_time` DATETIME(3) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_consultation_no` (`consultation_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_doctor_id` (`doctor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `doctor_consultation_reply` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `consultation_id` BIGINT NOT NULL,
  `consultation_no` VARCHAR(32) NOT NULL,
  `reply_role` VARCHAR(32) NOT NULL,
  `reply_user_id` BIGINT NOT NULL,
  `reply_user_name` VARCHAR(64) DEFAULT NULL,
  `reply_content` TEXT,
  `attachment_url` VARCHAR(512) DEFAULT NULL,
  `reply_time` DATETIME(3) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_consultation_no` (`consultation_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============ 系统 ============

CREATE TABLE `stat_daily_report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `stat_date` DATE NOT NULL,
  `center_code` VARCHAR(32) NOT NULL,
  `package_id` BIGINT DEFAULT NULL,
  `appointment_count` INT NOT NULL DEFAULT 0,
  `order_count` INT NOT NULL DEFAULT 0,
  `revenue` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stat_daily` (`stat_date`,`center_code`,`package_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `operator_id` BIGINT NOT NULL,
  `operator_type` VARCHAR(32) DEFAULT NULL,
  `module` VARCHAR(32) DEFAULT NULL,
  `action` VARCHAR(64) DEFAULT NULL,
  `target_type` VARCHAR(32) DEFAULT NULL,
  `target_id` VARCHAR(64) DEFAULT NULL,
  `detail` JSON DEFAULT NULL,
  `ip` VARCHAR(64) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_operator` (`operator_id`),
  KEY `idx_target` (`target_type`,`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `data_dictionary` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `dict_type` VARCHAR(64) NOT NULL,
  `dict_code` VARCHAR(64) NOT NULL,
  `dict_name` VARCHAR(128) NOT NULL,
  `sort_no` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `remark` VARCHAR(256) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_type_code` (`dict_type`,`dict_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `notification_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `notify_type` VARCHAR(32) NOT NULL,
  `title` VARCHAR(128) DEFAULT NULL,
  `content` TEXT,
  `read_status` TINYINT NOT NULL DEFAULT 0,
  `send_time` DATETIME(3) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`,`read_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
