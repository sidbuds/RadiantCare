DROP DATABASE IF EXISTS `xixin_health`;
CREATE DATABASE `xixin_health` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `xixin_health`;

CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_no` VARCHAR(32) NOT NULL COMMENT '',
  `name` VARCHAR(64) NOT NULL COMMENT '',
  `gender` TINYINT NOT NULL DEFAULT 0 COMMENT ''
  `birth_date` DATE DEFAULT NULL COMMENT '',
  `id_type` TINYINT DEFAULT NULL COMMENT '',
  `id_no` VARCHAR(64) DEFAULT NULL COMMENT '',
  `mobile` VARCHAR(20) NOT NULL COMMENT ''
  `email` VARCHAR(128) DEFAULT NULL COMMENT '',
  `password_hash` VARCHAR(128) DEFAULT NULL COMMENT '',
  `address` VARCHAR(256) DEFAULT NULL COMMENT '',
  `emergency_contact` VARCHAR(64) DEFAULT NULL COMMENT '',
  `emergency_mobile` VARCHAR(20) DEFAULT NULL COMMENT ''
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '',
  `last_login_at` DATETIME(3) DEFAULT NULL COMMENT ''
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_no` (`user_no`),
  UNIQUE KEY `uk_mobile` (`mobile`),
  UNIQUE KEY `uk_id_no` (`id_no`),
  KEY `idx_name_mobile` (`name`,`mobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户�?;

CREATE TABLE `staff_account` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(64) NOT NULL COMMENT '',
  `password_hash` VARCHAR(128) NOT NULL COMMENT '',
  `display_name` VARCHAR(64) NOT NULL COMMENT '',
  `bind_user_id` BIGINT NOT NULL COMMENT '',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '',
  `last_login_at` DATETIME(3) DEFAULT NULL COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_staff_account_username` (`username`),
  KEY `idx_bind_user_id` (`bind_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='��̨�˺ű�';

CREATE TABLE `staff_role_rel` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `staff_account_id` BIGINT NOT NULL COMMENT '',
  `role_code` VARCHAR(32) NOT NULL COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_staff_role` (`staff_account_id`,`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='��̨�˺Ž�ɫ������';

CREATE TABLE `health_profile` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '',
  `allergy_history` TEXT COMMENT ''
  `past_medical_history` TEXT COMMENT '',
  `family_history` TEXT COMMENT '',
  `medication_history` TEXT COMMENT ''
  `smoking_status` TINYINT NOT NULL DEFAULT 0 COMMENT ''
  `drinking_status` TINYINT NOT NULL DEFAULT 0 COMMENT ''
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_profile` (`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康档案�?;

CREATE TABLE `exam_package` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `package_code` VARCHAR(32) NOT NULL COMMENT '',
  `package_name` VARCHAR(128) NOT NULL COMMENT '',
  `category` VARCHAR(32) DEFAULT NULL COMMENT '',
  `price` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '',
  `remark` VARCHAR(512) DEFAULT NULL COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_package_code` (`package_code`),
  KEY `idx_package_name` (`package_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检套餐�?;

CREATE TABLE `exam_package_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `package_id` BIGINT NOT NULL COMMENT '',
  `item_code` VARCHAR(32) NOT NULL COMMENT '',
  `item_name` VARCHAR(128) NOT NULL COMMENT '',
  `unit` VARCHAR(32) DEFAULT NULL COMMENT '',
  `ref_range` VARCHAR(128) DEFAULT NULL COMMENT ''
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT ''
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_package_item` (`package_id`,`item_code`),
  KEY `idx_package_id` (`package_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐检查项�?;

CREATE TABLE `appointment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `appointment_no` VARCHAR(32) NOT NULL COMMENT '',
  `user_id` BIGINT NOT NULL COMMENT '',
  `package_id` BIGINT NOT NULL COMMENT '',
  `center_code` VARCHAR(32) NOT NULL COMMENT '',
  `appoint_date` DATE NOT NULL COMMENT '',
  `time_slot_code` VARCHAR(32) NOT NULL COMMENT '',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '',
  `cancel_reason` VARCHAR(256) DEFAULT NULL COMMENT '',
  `remark` VARCHAR(256) DEFAULT NULL COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_appointment_no` (`appointment_no`),
  KEY `idx_user_date` (`user_id`,`appoint_date`),
  KEY `idx_center_date_slot` (`center_code`,`appoint_date`,`time_slot_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约�?;

CREATE TABLE `appointment_queue` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `queue_no` VARCHAR(32) NOT NULL COMMENT '',
  `user_id` BIGINT NOT NULL COMMENT '',
  `package_id` BIGINT NOT NULL COMMENT '',
  `center_code` VARCHAR(32) NOT NULL COMMENT '',
  `appoint_date` DATE NOT NULL COMMENT '',
  `time_slot_code` VARCHAR(32) NOT NULL COMMENT '',
  `priority` INT NOT NULL DEFAULT 0 COMMENT ''
  `queue_status` TINYINT NOT NULL DEFAULT 0 COMMENT ''
  `expire_at` DATETIME(3) DEFAULT NULL COMMENT '',
  `confirmed_at` DATETIME(3) DEFAULT NULL COMMENT '',
  `remark` VARCHAR(256) DEFAULT NULL COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_queue_no` (`queue_no`),
  KEY `idx_queue_user_date` (`user_id`,`appoint_date`),
  KEY `idx_queue_center_slot` (`center_code`,`appoint_date`,`time_slot_code`,`queue_status`,`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ԤԼ�򲹱�';

CREATE TABLE `exam_center` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `center_code` VARCHAR(32) NOT NULL COMMENT '',
  `center_name` VARCHAR(128) NOT NULL COMMENT '',
  `address` VARCHAR(256) DEFAULT NULL COMMENT '',
  `phone` VARCHAR(32) DEFAULT NULL COMMENT '',
  `business_hours` VARCHAR(128) DEFAULT NULL COMMENT '',
  `description` TEXT DEFAULT NULL COMMENT '',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_center_code` (`center_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='������ı�';

CREATE TABLE `resource_capacity` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `center_code` VARCHAR(32) NOT NULL COMMENT '',
  `appoint_date` DATE NOT NULL COMMENT '',
  `time_slot_code` VARCHAR(32) NOT NULL COMMENT '',
  `resource_type` VARCHAR(32) NOT NULL COMMENT '',
  `resource_code` VARCHAR(64) NOT NULL COMMENT '',
  `capacity_total` INT NOT NULL DEFAULT 0 COMMENT ''
  `capacity_used` INT NOT NULL DEFAULT 0 COMMENT '',
  `capacity_locked` INT NOT NULL DEFAULT 0 COMMENT '',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '',
  `version_no` INT NOT NULL DEFAULT 0 COMMENT ''
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resource_capacity` (`center_code`,`appoint_date`,`time_slot_code`,`resource_type`,`resource_code`),
  KEY `idx_capacity_slot` (`center_code`,`appoint_date`,`time_slot_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源容量�?;

CREATE TABLE `resource_snapshot` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `snapshot_no` VARCHAR(32) NOT NULL COMMENT ''
  `appointment_no` VARCHAR(32) NOT NULL COMMENT '',
  `user_id` BIGINT NOT NULL COMMENT '',
  `package_id` BIGINT NOT NULL COMMENT '',
  `center_code` VARCHAR(32) NOT NULL COMMENT '',
  `appoint_date` DATE NOT NULL COMMENT '',
  `time_slot_code` VARCHAR(32) NOT NULL COMMENT '',
  `resource_type` VARCHAR(32) NOT NULL COMMENT '',
  `resource_code` VARCHAR(64) NOT NULL COMMENT '',
  `version_no` INT NOT NULL DEFAULT 0 COMMENT ''
  `lock_result` TINYINT NOT NULL DEFAULT 0 COMMENT '',
  `snapshot_data` TEXT COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_snapshot_no` (`snapshot_no`),
  KEY `idx_snapshot_appointment` (`appointment_no`),
  KEY `idx_snapshot_slot` (`center_code`,`appoint_date`,`time_slot_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源快照�?;

CREATE TABLE `schedule_resource_bind` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `center_code` VARCHAR(32) NOT NULL COMMENT '',
  `time_slot_code` VARCHAR(32) NOT NULL COMMENT '',
  `package_id` BIGINT NOT NULL COMMENT '',
  `item_code` VARCHAR(32) NOT NULL COMMENT '',
  `resource_type` VARCHAR(32) NOT NULL COMMENT '',
  `resource_code` VARCHAR(64) NOT NULL COMMENT '',
  `resource_name` VARCHAR(128) DEFAULT NULL COMMENT '',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_schedule_resource_bind` (`center_code`,`time_slot_code`,`package_id`,`item_code`,`resource_type`,`resource_code`),
  KEY `idx_bind_package` (`package_id`,`item_code`),
  KEY `idx_bind_resource` (`center_code`,`time_slot_code`,`resource_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='时段资源绑定�?;

CREATE TABLE `order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(32) NOT NULL COMMENT ''
  `appointment_id` BIGINT NOT NULL COMMENT '',
  `user_id` BIGINT NOT NULL COMMENT '',
  `package_id` BIGINT NOT NULL COMMENT '',
  `total_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT ''
  `pay_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '',
  `discount_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT ''
  `pay_channel` VARCHAR(32) DEFAULT NULL COMMENT '',
  `pay_time` DATETIME(3) DEFAULT NULL COMMENT '',
  `remark` VARCHAR(256) DEFAULT NULL COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  UNIQUE KEY `uk_appointment_id` (`appointment_id`),
  KEY `idx_user_status` (`user_id`,`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单�?;

CREATE TABLE `order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL COMMENT '',
  `item_type` TINYINT NOT NULL DEFAULT 0 COMMENT ''
  `ref_item_code` VARCHAR(32) NOT NULL COMMENT ''
  `ref_item_name` VARCHAR(128) NOT NULL COMMENT ''
  `qty` INT NOT NULL DEFAULT 1 COMMENT '',
  `unit_price` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '',
  `amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_ref_item` (`order_id`,`ref_item_code`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细�?;

CREATE TABLE `order_snapshot` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL COMMENT '',
  `order_no` VARCHAR(32) NOT NULL COMMENT ''
  `snapshot_type` VARCHAR(32) NOT NULL COMMENT '',
  `snapshot_content` TEXT NOT NULL COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_order_snapshot` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单快照�?;

CREATE TABLE `order_settlement_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL COMMENT '',
  `order_item_id` BIGINT NOT NULL COMMENT '',
  `settlement_type` VARCHAR(32) NOT NULL COMMENT '',
  `origin_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '',
  `discount_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '',
  `settlement_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '',
  `remark` VARCHAR(256) DEFAULT NULL COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_order_item` (`order_id`,`order_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单结算明细�?;

CREATE TABLE `payment_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `payment_no` VARCHAR(32) NOT NULL COMMENT ''
  `order_id` BIGINT NOT NULL COMMENT '',
  `order_no` VARCHAR(32) NOT NULL COMMENT ''
  `channel` VARCHAR(32) NOT NULL COMMENT '',
  `trade_no` VARCHAR(64) DEFAULT NULL COMMENT '',
  `pay_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '',
  `pay_time` DATETIME(3) DEFAULT NULL COMMENT '',
  `callback_time` DATETIME(3) DEFAULT NULL COMMENT '',
  `raw_payload` TEXT COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_payment_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付流水�?;

CREATE TABLE `refund_apply` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `apply_no` VARCHAR(32) NOT NULL COMMENT ''
  `order_id` BIGINT NOT NULL COMMENT '',
  `order_no` VARCHAR(32) NOT NULL COMMENT ''
  `refund_type` VARCHAR(32) NOT NULL COMMENT ''
  `apply_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT ''
  `reason` VARCHAR(256) DEFAULT NULL COMMENT '',
  `apply_status` TINYINT NOT NULL DEFAULT 0 COMMENT ''
  `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT ''
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_apply_no` (`apply_no`),
  KEY `idx_refund_apply_order` (`order_id`,`apply_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款申请表';

CREATE TABLE `refund_audit_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `refund_apply_id` BIGINT NOT NULL COMMENT '',
  `apply_no` VARCHAR(32) NOT NULL COMMENT ''
  `audit_action` VARCHAR(32) NOT NULL COMMENT '',
  `audit_result` VARCHAR(32) NOT NULL COMMENT '',
  `audit_remark` VARCHAR(512) DEFAULT NULL COMMENT '',
  `operator_id` BIGINT DEFAULT NULL COMMENT '',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT ''
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_refund_apply_id` (`refund_apply_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款审核日志表';

CREATE TABLE `refund_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `refund_no` VARCHAR(32) NOT NULL COMMENT '',
  `refund_apply_id` BIGINT NOT NULL COMMENT '',
  `order_id` BIGINT NOT NULL COMMENT '',
  `order_no` VARCHAR(32) NOT NULL COMMENT ''
  `channel` VARCHAR(32) NOT NULL COMMENT ''
  `channel_refund_no` VARCHAR(64) DEFAULT NULL COMMENT ''
  `refund_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT ''
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '',
  `refund_time` DATETIME(3) DEFAULT NULL COMMENT ''
  `raw_payload` TEXT COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_refund_apply_id` (`refund_apply_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款执行结果表';

CREATE TABLE `exam_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `task_no` VARCHAR(32) NOT NULL COMMENT ''
  `appointment_id` BIGINT NOT NULL COMMENT '',
  `appointment_no` VARCHAR(32) NOT NULL COMMENT '',
  `order_id` BIGINT NOT NULL COMMENT '',
  `order_no` VARCHAR(32) NOT NULL COMMENT ''
  `user_id` BIGINT NOT NULL COMMENT '',
  `package_id` BIGINT NOT NULL COMMENT '',
  `center_code` VARCHAR(32) NOT NULL COMMENT '',
  `task_date` DATE NOT NULL COMMENT '',
  `task_status` TINYINT NOT NULL DEFAULT 0 COMMENT ''
  `report_status` TINYINT NOT NULL DEFAULT 0 COMMENT ''
  `guide_status` TINYINT NOT NULL DEFAULT 0 COMMENT ''
  `arrive_time` DATETIME(3) DEFAULT NULL COMMENT '',
  `start_time` DATETIME(3) DEFAULT NULL COMMENT ''
  `complete_time` DATETIME(3) DEFAULT NULL COMMENT '',
  `remark` VARCHAR(256) DEFAULT NULL COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_no` (`task_no`),
  UNIQUE KEY `uk_exam_task_appointment_id` (`appointment_id`),
  KEY `idx_exam_task_user_status` (`user_id`,`task_status`),
  KEY `idx_exam_task_center_date` (`center_code`,`task_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检任务�?;

CREATE TABLE `exam_department_route` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `route_code` VARCHAR(32) NOT NULL COMMENT '',
  `center_code` VARCHAR(32) NOT NULL COMMENT '',
  `package_id` BIGINT NOT NULL COMMENT '',
  `item_code` VARCHAR(32) NOT NULL COMMENT '',
  `department_code` VARCHAR(32) NOT NULL COMMENT '',
  `department_name` VARCHAR(128) NOT NULL COMMENT '',
  `room_no` VARCHAR(64) DEFAULT NULL COMMENT ''
  `floor_no` VARCHAR(32) DEFAULT NULL COMMENT '',
  `building_no` VARCHAR(32) DEFAULT NULL COMMENT '',
  `route_sort` INT NOT NULL DEFAULT 0 COMMENT '',
  `guide_text` VARCHAR(512) DEFAULT NULL COMMENT '',
  `need_empty_stomach` TINYINT NOT NULL DEFAULT 0 COMMENT '',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_route_code` (`route_code`),
  UNIQUE KEY `uk_route_package_item` (`center_code`,`package_id`,`item_code`),
  KEY `idx_route_center_package` (`center_code`,`package_id`,`route_sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导检路线�?;

CREATE TABLE `exam_task_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `task_item_no` VARCHAR(32) NOT NULL COMMENT '',
  `task_id` BIGINT NOT NULL COMMENT '',
  `task_no` VARCHAR(32) NOT NULL COMMENT ''
  `item_code` VARCHAR(32) NOT NULL COMMENT '',
  `item_name` VARCHAR(128) NOT NULL COMMENT '',
  `department_code` VARCHAR(32) NOT NULL COMMENT '',
  `department_name` VARCHAR(128) NOT NULL COMMENT '',
  `doctor_id` BIGINT DEFAULT NULL COMMENT '',
  `doctor_name` VARCHAR(64) DEFAULT NULL COMMENT '',
  `room_no` VARCHAR(64) DEFAULT NULL COMMENT ''
  `route_sort` INT NOT NULL DEFAULT 0 COMMENT '',
  `item_status` TINYINT NOT NULL DEFAULT 0 COMMENT '',
  `entry_status` TINYINT NOT NULL DEFAULT 0 COMMENT ''
  `start_time` DATETIME(3) DEFAULT NULL COMMENT ''
  `complete_time` DATETIME(3) DEFAULT NULL COMMENT '',
  `skip_reason` VARCHAR(256) DEFAULT NULL COMMENT '',
  `remark` VARCHAR(256) DEFAULT NULL COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_item_no` (`task_item_no`),
  UNIQUE KEY `uk_task_item_unique` (`task_id`,`item_code`),
  KEY `idx_task_route` (`task_id`,`route_sort`),
  KEY `idx_doctor_status` (`doctor_id`,`item_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检任务项表';

CREATE TABLE `exam_result` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `result_no` VARCHAR(32) NOT NULL COMMENT ''
  `task_id` BIGINT NOT NULL COMMENT '',
  `task_item_id` BIGINT NOT NULL COMMENT '',
  `task_item_no` VARCHAR(32) NOT NULL COMMENT '',
  `user_id` BIGINT NOT NULL COMMENT '',
  `report_id` BIGINT DEFAULT NULL COMMENT '',
  `item_code` VARCHAR(32) NOT NULL COMMENT '',
  `item_name` VARCHAR(128) NOT NULL COMMENT '',
  `result_type` VARCHAR(32) NOT NULL COMMENT '',
  `result_value` VARCHAR(256) DEFAULT NULL COMMENT ''
  `result_number` DECIMAL(18,4) DEFAULT NULL COMMENT ''
  `unit` VARCHAR(32) DEFAULT NULL COMMENT '',
  `ref_range` VARCHAR(128) DEFAULT NULL COMMENT ''
  `conclusion` VARCHAR(512) DEFAULT NULL COMMENT ''
  `is_abnormal` TINYINT NOT NULL DEFAULT 0 COMMENT '',
  `abnormal_level` TINYINT NOT NULL DEFAULT 0 COMMENT '',
  `entry_doctor_id` BIGINT DEFAULT NULL COMMENT '',
  `entry_doctor_name` VARCHAR(64) DEFAULT NULL COMMENT '',
  `entry_time` DATETIME(3) DEFAULT NULL COMMENT '',
  `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT ''
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_result_no` (`result_no`),
  KEY `idx_result_task_item` (`task_item_id`),
  KEY `idx_result_user_item` (`user_id`,`item_code`),
  KEY `idx_result_report_id` (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检结果�?;

CREATE TABLE `exam_result_attachment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `attachment_no` VARCHAR(32) NOT NULL COMMENT ''
  `result_id` BIGINT NOT NULL COMMENT '',
  `task_item_id` BIGINT NOT NULL COMMENT '',
  `file_name` VARCHAR(256) NOT NULL COMMENT ''
  `file_url` VARCHAR(512) NOT NULL COMMENT '',
  `file_type` VARCHAR(64) DEFAULT NULL COMMENT '',
  `file_size` BIGINT DEFAULT NULL COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_attachment_no` (`attachment_no`),
  KEY `idx_result_id` (`result_id`),
  KEY `idx_task_item_id` (`task_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检结果附件�?;

CREATE TABLE `report_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `template_code` VARCHAR(32) NOT NULL COMMENT '',
  `template_name` VARCHAR(128) NOT NULL COMMENT '',
  `package_id` BIGINT NOT NULL COMMENT '',
  `template_type` VARCHAR(32) NOT NULL COMMENT '',
  `version_no` INT NOT NULL DEFAULT 1 COMMENT ''
  `render_engine` VARCHAR(32) DEFAULT NULL COMMENT '',
  `template_config` TEXT COMMENT '',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_code` (`template_code`),
  KEY `idx_package_status` (`package_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报告模板�?;

CREATE TABLE `report_section_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `template_id` BIGINT NOT NULL COMMENT '',
  `section_code` VARCHAR(32) NOT NULL COMMENT '',
  `section_name` VARCHAR(128) NOT NULL COMMENT '',
  `section_type` VARCHAR(32) NOT NULL COMMENT '',
  `data_source_type` VARCHAR(32) NOT NULL COMMENT '',
  `item_codes` TEXT COMMENT '',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT ''
  `render_rule` TEXT COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_section` (`template_id`,`section_code`),
  KEY `idx_template_sort` (`template_id`,`sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报告模板章节�?;

CREATE TABLE `exam_report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `report_no` VARCHAR(32) NOT NULL COMMENT '',
  `appointment_id` BIGINT NOT NULL COMMENT '',
  `task_id` BIGINT NOT NULL COMMENT '',
  `user_id` BIGINT NOT NULL COMMENT '',
  `package_id` BIGINT NOT NULL COMMENT '',
  `template_id` BIGINT DEFAULT NULL COMMENT '',
  `report_date` DATE NOT NULL COMMENT '',
  `overall_conclusion` TEXT COMMENT '',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT ''
  `pdf_url` VARCHAR(512) DEFAULT NULL COMMENT '',
  `version_no` INT NOT NULL DEFAULT 1 COMMENT ''
  `published_at` DATETIME(3) DEFAULT NULL COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_report_no` (`report_no`),
  UNIQUE KEY `uk_exam_report_appointment_id` (`appointment_id`),
  UNIQUE KEY `uk_exam_report_task_id` (`task_id`),
  KEY `idx_user_report_date` (`user_id`,`report_date`),
  KEY `idx_report_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检报告�?;

CREATE TABLE `exam_report_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `report_id` BIGINT NOT NULL COMMENT '',
  `item_code` VARCHAR(32) NOT NULL COMMENT '',
  `item_name` VARCHAR(128) NOT NULL COMMENT '',
  `result_value` VARCHAR(256) DEFAULT NULL COMMENT ''
  `result_number` DECIMAL(18,4) DEFAULT NULL COMMENT ''
  `unit` VARCHAR(32) DEFAULT NULL COMMENT '',
  `ref_range` VARCHAR(128) DEFAULT NULL COMMENT ''
  `is_abnormal` TINYINT NOT NULL DEFAULT 0 COMMENT '',
  `abnormal_level` TINYINT NOT NULL DEFAULT 0 COMMENT '',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT ''
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_report_item` (`report_id`,`item_code`),
  KEY `idx_report_id` (`report_id`),
  KEY `idx_item_code` (`item_code`),
  KEY `idx_abnormal` (`is_abnormal`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报告指标�?;

CREATE TABLE `exam_report_item_abnormal` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `report_id` BIGINT NOT NULL COMMENT '',
  `report_item_id` BIGINT NOT NULL COMMENT '',
  `abnormal_code` VARCHAR(32) NOT NULL COMMENT '',
  `abnormal_name` VARCHAR(128) NOT NULL COMMENT '',
  `abnormal_level` TINYINT NOT NULL DEFAULT 1 COMMENT '',
  `suggestion` VARCHAR(512) DEFAULT NULL COMMENT '',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT ''
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_report_item_abnormal` (`report_item_id`,`abnormal_code`),
  KEY `idx_report_id` (`report_id`),
  KEY `idx_level_status` (`abnormal_level`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报告异常项表';

CREATE TABLE `doctor_review_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `review_no` VARCHAR(32) NOT NULL COMMENT ''
  `report_id` BIGINT NOT NULL COMMENT '',
  `task_id` BIGINT NOT NULL COMMENT '',
  `review_stage` VARCHAR(32) NOT NULL COMMENT '',
  `review_status` VARCHAR(32) NOT NULL COMMENT '',
  `review_comment` VARCHAR(512) DEFAULT NULL COMMENT '',
  `reviewer_id` BIGINT DEFAULT NULL COMMENT '',
  `reviewer_name` VARCHAR(64) DEFAULT NULL COMMENT ''
  `reviewed_at` DATETIME(3) DEFAULT NULL COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_review_no` (`review_no`),
  KEY `idx_report_stage` (`report_id`,`review_stage`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生审核记录�?;

CREATE TABLE `report_compare_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `task_no` VARCHAR(32) NOT NULL COMMENT ''
  `user_id` BIGINT NOT NULL COMMENT '',
  `baseline_report_id` BIGINT NOT NULL COMMENT '',
  `compare_report_id` BIGINT NOT NULL COMMENT '',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_no` (`task_no`),
  UNIQUE KEY `uk_compare_pair` (`user_id`,`baseline_report_id`,`compare_report_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='历年对比任务�?;

CREATE TABLE `report_compare_result` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `task_id` BIGINT NOT NULL COMMENT '',
  `item_code` VARCHAR(32) NOT NULL COMMENT '',
  `item_name` VARCHAR(128) NOT NULL COMMENT '',
  `base_value` VARCHAR(256) DEFAULT NULL COMMENT ''
  `compare_value` VARCHAR(256) DEFAULT NULL COMMENT ''
  `change_value` DECIMAL(18,4) DEFAULT NULL COMMENT ''
  `change_rate` DECIMAL(10,4) DEFAULT NULL COMMENT ''
  `trend` TINYINT NOT NULL DEFAULT 0 COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_item` (`task_id`,`item_code`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='历年对比结果�?;

CREATE TABLE `report_indicator_yearly` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '',
  `report_id` BIGINT NOT NULL COMMENT '',
  `item_code` VARCHAR(32) NOT NULL COMMENT '',
  `item_name` VARCHAR(128) NOT NULL COMMENT '',
  `stat_year` INT NOT NULL COMMENT '',
  `result_number` DECIMAL(18,4) DEFAULT NULL COMMENT ''
  `unit` VARCHAR(32) DEFAULT NULL COMMENT '',
  `abnormal_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_yearly_indicator` (`report_id`,`item_code`),
  KEY `idx_user_item_year` (`user_id`,`item_code`,`stat_year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='年度指标标准化表';

CREATE TABLE `indicator_trend_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `task_id` BIGINT NOT NULL COMMENT '',
  `item_code` VARCHAR(32) NOT NULL COMMENT '',
  `trend_tag` VARCHAR(64) NOT NULL COMMENT '',
  `trend_direction` TINYINT NOT NULL DEFAULT 0 COMMENT '',
  `turning_year` INT DEFAULT NULL COMMENT '',
  `risk_level` TINYINT NOT NULL DEFAULT 0 COMMENT '',
  `tag_reason` VARCHAR(512) DEFAULT NULL COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_indicator_tag` (`task_id`,`item_code`),
  KEY `idx_item_code` (`item_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标趋势标签�?;

CREATE TABLE `health_risk_score` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `task_id` BIGINT NOT NULL COMMENT '',
  `user_id` BIGINT NOT NULL COMMENT '',
  `score_total` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '',
  `score_abnormal` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '',
  `score_trend` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '',
  `risk_level` TINYINT NOT NULL DEFAULT 0 COMMENT '',
  `score_detail` TEXT COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_score` (`task_id`),
  KEY `idx_user_risk` (`user_id`,`risk_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康风险评分�?;

CREATE TABLE `health_advice_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `advice_no` VARCHAR(32) NOT NULL COMMENT '',
  `user_id` BIGINT NOT NULL COMMENT '',
  `report_id` BIGINT DEFAULT NULL COMMENT '',
  `compare_task_id` BIGINT DEFAULT NULL COMMENT '',
  `source_type` VARCHAR(32) NOT NULL COMMENT '',
  `advice_type` VARCHAR(32) NOT NULL COMMENT '',
  `risk_level` TINYINT NOT NULL DEFAULT 0 COMMENT '',
  `advice_title` VARCHAR(128) NOT NULL COMMENT '',
  `advice_content` TEXT NOT NULL COMMENT '',
  `action_suggestion` VARCHAR(256) DEFAULT NULL COMMENT '',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_advice_no` (`advice_no`),
  KEY `idx_user_risk_created` (`user_id`,`risk_level`,`created_at`),
  KEY `idx_report_id` (`report_id`),
  KEY `idx_compare_task_id` (`compare_task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康建议记录�?;

CREATE TABLE `stat_daily_report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `stat_date` DATE NOT NULL COMMENT '',
  `center_code` VARCHAR(32) NOT NULL COMMENT '',
  `package_id` BIGINT NOT NULL COMMENT '',
  `appointment_cnt` INT NOT NULL DEFAULT 0 COMMENT ''
  `completed_cnt` INT NOT NULL DEFAULT 0 COMMENT ''
  `cancel_cnt` INT NOT NULL DEFAULT 0 COMMENT ''
  `order_cnt` INT NOT NULL DEFAULT 0 COMMENT ''
  `pay_success_cnt` INT NOT NULL DEFAULT 0 COMMENT ''
  `refund_cnt` INT NOT NULL DEFAULT 0 COMMENT '',
  `abnormal_report_cnt` INT NOT NULL DEFAULT 0 COMMENT ''
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stat_daily` (`stat_date`,`center_code`,`package_id`),
  KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运营日报统计�?;

CREATE TABLE `doctor_consultation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `consultation_no` VARCHAR(32) NOT NULL COMMENT '',
  `user_id` BIGINT NOT NULL COMMENT '',
  `doctor_id` BIGINT DEFAULT NULL COMMENT '',
  `doctor_name` VARCHAR(64) DEFAULT NULL COMMENT '',
  `report_id` BIGINT DEFAULT NULL COMMENT '',
  `report_no` VARCHAR(32) DEFAULT NULL COMMENT '',
  `source_type` VARCHAR(32) NOT NULL COMMENT '',
  `consultation_type` VARCHAR(32) NOT NULL COMMENT '',
  `consultation_title` VARCHAR(128) DEFAULT NULL COMMENT '',
  `consultation_content` TEXT NOT NULL COMMENT '',
  `consultation_status` TINYINT NOT NULL DEFAULT 0 COMMENT '',
  `priority_level` TINYINT NOT NULL DEFAULT 0 COMMENT '',
  `latest_reply_time` DATETIME(3) DEFAULT NULL COMMENT '',
  `closed_time` DATETIME(3) DEFAULT NULL COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_consultation_no` (`consultation_no`),
  KEY `idx_consultation_user_created` (`user_id`,`created_at`),
  KEY `idx_consultation_report_created` (`report_id`,`created_at`),
  KEY `idx_consultation_doctor_status` (`doctor_id`,`consultation_status`),
  KEY `idx_consultation_status_created` (`consultation_status`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ҽ����ѯ����';

CREATE TABLE `doctor_consultation_reply` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `consultation_id` BIGINT NOT NULL COMMENT '',
  `consultation_no` VARCHAR(32) NOT NULL COMMENT '',
  `reply_role` VARCHAR(16) NOT NULL COMMENT '',
  `reply_user_id` BIGINT DEFAULT NULL COMMENT '',
  `reply_user_name` VARCHAR(64) DEFAULT NULL COMMENT '',
  `reply_content` TEXT NOT NULL COMMENT '',
  `attachment_url` VARCHAR(512) DEFAULT NULL COMMENT '',
  `reply_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_reply_consultation_time` (`consultation_id`,`reply_time`),
  KEY `idx_reply_consultation_no` (`consultation_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ҽ����ѯ�ظ���';

CREATE TABLE `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `biz_type` VARCHAR(32) NOT NULL COMMENT '',
  `biz_id` VARCHAR(64) NOT NULL COMMENT '',
  `action` VARCHAR(32) NOT NULL COMMENT '',
  `operator_id` BIGINT NOT NULL COMMENT '',
  `operator_name` VARCHAR(64) NOT NULL COMMENT ''
  `ip` VARCHAR(64) DEFAULT NULL COMMENT '',
  `before_data` TEXT COMMENT ''
  `after_data` TEXT COMMENT ''
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_biz` (`biz_type`,`biz_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志�?;

CREATE TABLE `data_dictionary` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `dict_type` VARCHAR(64) NOT NULL COMMENT '',
  `dict_code` VARCHAR(64) NOT NULL COMMENT '',
  `dict_name` VARCHAR(128) NOT NULL COMMENT '',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT ''
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT ''
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_type_code` (`dict_type`,`dict_code`),
  KEY `idx_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典�?;

CREATE TABLE `notification_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '',
  `channel` VARCHAR(32) NOT NULL COMMENT '',
  `template_code` VARCHAR(64) NOT NULL COMMENT '',
  `biz_type` VARCHAR(32) NOT NULL COMMENT '',
  `biz_id` VARCHAR(64) NOT NULL COMMENT '',
  `content` TEXT NOT NULL COMMENT '',
  `send_status` TINYINT NOT NULL DEFAULT 0 COMMENT ''
  `send_at` DATETIME(3) DEFAULT NULL COMMENT ''
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_channel` (`user_id`,`channel`),
  KEY `idx_biz` (`biz_type`,`biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知记录�?;
