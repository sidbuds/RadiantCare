-- ============================================
-- 熙心健康体检平台 - SQL转ER图用建表语句
-- 用途：导入SQL转ER图工具（如dbdiagram.io、sql-workbench等）
-- ============================================

-- ============================================
-- 1. 用户体系
-- ============================================
CREATE TABLE `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_no` VARCHAR(32) NOT NULL UNIQUE COMMENT '用户编号',
  `name` VARCHAR(64) NOT NULL COMMENT '姓名',
  `gender` TINYINT DEFAULT 0 COMMENT '性别',
  `mobile` VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
  `password_hash` VARCHAR(128) COMMENT '密码哈希',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1启用 0停用'
);

CREATE TABLE `staff_account` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(64) NOT NULL UNIQUE COMMENT '用户名',
  `password_hash` VARCHAR(128) NOT NULL COMMENT '密码哈希',
  `display_name` VARCHAR(64) NOT NULL COMMENT '显示名称',
  `bind_user_id` BIGINT NOT NULL COMMENT '绑定用户ID',
  `status` TINYINT DEFAULT 1 COMMENT '状态',
  FOREIGN KEY (`bind_user_id`) REFERENCES `user`(`id`)
);

CREATE TABLE `staff_role_rel` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `staff_account_id` BIGINT NOT NULL COMMENT '后台账号ID',
  `role_code` VARCHAR(32) NOT NULL COMMENT '角色编码：DOCTOR/ADMIN/OPERATOR',
  FOREIGN KEY (`staff_account_id`) REFERENCES `staff_account`(`id`)
);

-- ============================================
-- 2. 预约套餐
-- ============================================
CREATE TABLE `exam_package` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `package_code` VARCHAR(32) NOT NULL UNIQUE COMMENT '套餐编码',
  `package_name` VARCHAR(128) NOT NULL COMMENT '套餐名称',
  `category` VARCHAR(32) COMMENT '分类',
  `price` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '价格',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1上架 0下架'
);

CREATE TABLE `exam_package_item` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `package_id` BIGINT NOT NULL COMMENT '套餐ID',
  `item_code` VARCHAR(32) NOT NULL COMMENT '项目编码',
  `item_name` VARCHAR(128) NOT NULL COMMENT '项目名称',
  `unit` VARCHAR(32) COMMENT '单位',
  `ref_range` VARCHAR(128) COMMENT '参考范围',
  `sort_no` INT DEFAULT 0 COMMENT '排序号',
  FOREIGN KEY (`package_id`) REFERENCES `exam_package`(`id`)
);

CREATE TABLE `resource_capacity` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `center_code` VARCHAR(32) NOT NULL COMMENT '中心编码',
  `appoint_date` DATE NOT NULL COMMENT '预约日期',
  `time_slot_code` VARCHAR(32) NOT NULL COMMENT '时段编码',
  `capacity_total` INT DEFAULT 0 COMMENT '总容量',
  `capacity_used` INT DEFAULT 0 COMMENT '已使用',
  `status` TINYINT DEFAULT 1 COMMENT '状态'
);

CREATE TABLE `appointment` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `appointment_no` VARCHAR(32) NOT NULL UNIQUE COMMENT '预约单号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `package_id` BIGINT NOT NULL COMMENT '套餐ID',
  `center_code` VARCHAR(32) NOT NULL COMMENT '中心编码',
  `appoint_date` DATE NOT NULL COMMENT '预约日期',
  `time_slot_code` VARCHAR(32) NOT NULL COMMENT '时段编码',
  `status` TINYINT DEFAULT 0 COMMENT '状态',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`package_id`) REFERENCES `exam_package`(`id`)
);

-- ============================================
-- 3. 订单支付
-- ============================================
CREATE TABLE `order` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `order_no` VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
  `appointment_id` BIGINT NOT NULL COMMENT '预约ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `package_id` BIGINT NOT NULL COMMENT '套餐ID',
  `total_amount` DECIMAL(12,2) DEFAULT 0 COMMENT '总金额',
  `pay_amount` DECIMAL(12,2) DEFAULT 0 COMMENT '实付金额',
  `discount_amount` DECIMAL(12,2) DEFAULT 0 COMMENT '优惠金额',
  `status` TINYINT DEFAULT 0 COMMENT '状态',
  `pay_channel` VARCHAR(32) COMMENT '支付渠道',
  FOREIGN KEY (`appointment_id`) REFERENCES `appointment`(`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
);

CREATE TABLE `order_item` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `item_type` TINYINT DEFAULT 0 COMMENT '类型：0套餐项 1附加项',
  `ref_item_code` VARCHAR(32) NOT NULL COMMENT '项目编码',
  `ref_item_name` VARCHAR(128) NOT NULL COMMENT '项目名称',
  `qty` INT DEFAULT 1 COMMENT '数量',
  `unit_price` DECIMAL(12,2) DEFAULT 0 COMMENT '单价',
  `amount` DECIMAL(12,2) DEFAULT 0 COMMENT '金额',
  FOREIGN KEY (`order_id`) REFERENCES `order`(`id`)
);

CREATE TABLE `payment_record` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `payment_no` VARCHAR(32) NOT NULL UNIQUE COMMENT '支付流水号',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `channel` VARCHAR(32) NOT NULL COMMENT '支付渠道',
  `trade_no` VARCHAR(64) COMMENT '第三方交易号',
  `pay_amount` DECIMAL(12,2) DEFAULT 0 COMMENT '支付金额',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0待支付 1成功 2失败',
  FOREIGN KEY (`order_id`) REFERENCES `order`(`id`)
);

-- ============================================
-- 4. 体检执行
-- ============================================
CREATE TABLE `exam_task` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `task_no` VARCHAR(32) NOT NULL UNIQUE COMMENT '任务号',
  `appointment_id` BIGINT NOT NULL COMMENT '预约ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `task_date` DATE NOT NULL COMMENT '体检日期',
  `task_status` TINYINT DEFAULT 0 COMMENT '任务状态',
  `report_status` TINYINT DEFAULT 0 COMMENT '报告状态',
  FOREIGN KEY (`appointment_id`) REFERENCES `appointment`(`id`),
  FOREIGN KEY (`order_id`) REFERENCES `order`(`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
);

CREATE TABLE `exam_department_route` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `route_code` VARCHAR(32) NOT NULL UNIQUE COMMENT '路线编码',
  `center_code` VARCHAR(32) NOT NULL COMMENT '中心编码',
  `package_id` BIGINT NOT NULL COMMENT '套餐ID',
  `item_code` VARCHAR(32) NOT NULL COMMENT '项目编码',
  `department_code` VARCHAR(32) NOT NULL COMMENT '科室编码',
  `department_name` VARCHAR(128) NOT NULL COMMENT '科室名称',
  `room_no` VARCHAR(64) COMMENT '房间号',
  `route_sort` INT DEFAULT 0 COMMENT '检查顺序',
  FOREIGN KEY (`package_id`) REFERENCES `exam_package`(`id`)
);

CREATE TABLE `exam_task_item` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `task_item_no` VARCHAR(32) NOT NULL UNIQUE COMMENT '任务项号',
  `task_id` BIGINT NOT NULL COMMENT '任务ID',
  `item_code` VARCHAR(32) NOT NULL COMMENT '项目编码',
  `item_name` VARCHAR(128) NOT NULL COMMENT '项目名称',
  `department_code` VARCHAR(32) NOT NULL COMMENT '科室编码',
  `doctor_id` BIGINT COMMENT '医生ID',
  `item_status` TINYINT DEFAULT 0 COMMENT '项目状态',
  FOREIGN KEY (`task_id`) REFERENCES `exam_task`(`id`),
  FOREIGN KEY (`doctor_id`) REFERENCES `staff_account`(`id`)
);

CREATE TABLE `exam_result` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `result_no` VARCHAR(32) NOT NULL UNIQUE COMMENT '结果号',
  `task_id` BIGINT NOT NULL COMMENT '任务ID',
  `task_item_id` BIGINT NOT NULL COMMENT '任务项ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `item_code` VARCHAR(32) NOT NULL COMMENT '项目编码',
  `result_type` VARCHAR(32) NOT NULL COMMENT '结果类型',
  `result_value` VARCHAR(256) COMMENT '结果文本值',
  `result_number` DECIMAL(18,4) COMMENT '结果数值',
  `is_abnormal` TINYINT DEFAULT 0 COMMENT '是否异常',
  `abnormal_level` TINYINT DEFAULT 0 COMMENT '异常级别',
  `entry_doctor_id` BIGINT COMMENT '录入医生ID',
  FOREIGN KEY (`task_id`) REFERENCES `exam_task`(`id`),
  FOREIGN KEY (`task_item_id`) REFERENCES `exam_task_item`(`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`entry_doctor_id`) REFERENCES `staff_account`(`id`)
);

-- ============================================
-- 5. 报告管理
-- ============================================
CREATE TABLE `report_template` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `template_code` VARCHAR(32) NOT NULL UNIQUE COMMENT '模板编码',
  `template_name` VARCHAR(128) NOT NULL COMMENT '模板名称',
  `package_id` BIGINT NOT NULL COMMENT '套餐ID',
  `template_type` VARCHAR(32) NOT NULL COMMENT '模板类型',
  `status` TINYINT DEFAULT 1 COMMENT '状态',
  FOREIGN KEY (`package_id`) REFERENCES `exam_package`(`id`)
);

CREATE TABLE `exam_report` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `report_no` VARCHAR(32) NOT NULL UNIQUE COMMENT '报告编号',
  `appointment_id` BIGINT NOT NULL COMMENT '预约ID',
  `task_id` BIGINT NOT NULL COMMENT '任务ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `package_id` BIGINT NOT NULL COMMENT '套餐ID',
  `template_id` BIGINT COMMENT '模板ID',
  `report_date` DATE NOT NULL COMMENT '报告日期',
  `overall_conclusion` TEXT COMMENT '总体结论',
  `status` TINYINT DEFAULT 0 COMMENT '状态',
  FOREIGN KEY (`appointment_id`) REFERENCES `appointment`(`id`),
  FOREIGN KEY (`task_id`) REFERENCES `exam_task`(`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`template_id`) REFERENCES `report_template`(`id`)
);

CREATE TABLE `exam_report_item` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `report_id` BIGINT NOT NULL COMMENT '报告ID',
  `item_code` VARCHAR(32) NOT NULL COMMENT '项目编码',
  `item_name` VARCHAR(128) NOT NULL COMMENT '项目名称',
  `result_value` VARCHAR(256) COMMENT '结果值',
  `result_number` DECIMAL(18,4) COMMENT '结果数值',
  `is_abnormal` TINYINT DEFAULT 0 COMMENT '是否异常',
  `abnormal_level` TINYINT DEFAULT 0 COMMENT '异常级别',
  `sort_no` INT DEFAULT 0 COMMENT '排序号',
  FOREIGN KEY (`report_id`) REFERENCES `exam_report`(`id`)
);

CREATE TABLE `doctor_review_record` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `review_no` VARCHAR(32) NOT NULL UNIQUE COMMENT '审核流水号',
  `report_id` BIGINT NOT NULL COMMENT '报告ID',
  `task_id` BIGINT NOT NULL COMMENT '任务ID',
  `review_stage` VARCHAR(32) NOT NULL COMMENT '审核阶段',
  `review_status` VARCHAR(32) NOT NULL COMMENT '审核结果',
  `review_comment` VARCHAR(512) COMMENT '审核意见',
  `reviewer_id` BIGINT COMMENT '审核人ID',
  FOREIGN KEY (`report_id`) REFERENCES `exam_report`(`id`),
  FOREIGN KEY (`task_id`) REFERENCES `exam_task`(`id`),
  FOREIGN KEY (`reviewer_id`) REFERENCES `staff_account`(`id`)
);

-- ============================================
-- 6. 咨询管理
-- ============================================
CREATE TABLE `doctor_consultation` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `consultation_no` VARCHAR(32) NOT NULL UNIQUE COMMENT '咨询单号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `doctor_id` BIGINT COMMENT '接诊医生ID',
  `report_id` BIGINT COMMENT '关联报告ID',
  `source_type` VARCHAR(32) NOT NULL COMMENT '来源页面',
  `consultation_type` VARCHAR(32) NOT NULL COMMENT '咨询类型',
  `consultation_title` VARCHAR(128) COMMENT '咨询标题',
  `consultation_content` TEXT NOT NULL COMMENT '咨询内容',
  `consultation_status` TINYINT DEFAULT 0 COMMENT '状态',
  `priority_level` TINYINT DEFAULT 0 COMMENT '优先级',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`doctor_id`) REFERENCES `staff_account`(`id`),
  FOREIGN KEY (`report_id`) REFERENCES `exam_report`(`id`)
);

CREATE TABLE `doctor_consultation_reply` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `consultation_id` BIGINT NOT NULL COMMENT '咨询ID',
  `consultation_no` VARCHAR(32) NOT NULL COMMENT '咨询单号',
  `reply_role` VARCHAR(16) NOT NULL COMMENT '回复角色：USER/DOCTOR/SYSTEM',
  `reply_user_id` BIGINT COMMENT '回复人ID',
  `reply_user_name` VARCHAR(64) COMMENT '回复人姓名',
  `reply_content` TEXT NOT NULL COMMENT '回复内容',
  `attachment_url` VARCHAR(512) COMMENT '附件地址',
  `reply_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '回复时间',
  FOREIGN KEY (`consultation_id`) REFERENCES `doctor_consultation`(`id`),
  FOREIGN KEY (`reply_user_id`) REFERENCES `user`(`id`)
);

-- ============================================
-- 7. 数据分析
-- ============================================
CREATE TABLE `report_compare_task` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `task_no` VARCHAR(32) NOT NULL UNIQUE COMMENT '任务号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `baseline_report_id` BIGINT NOT NULL COMMENT '基线报告ID',
  `compare_report_id` BIGINT NOT NULL COMMENT '对比报告ID',
  `status` TINYINT DEFAULT 0 COMMENT '状态',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`baseline_report_id`) REFERENCES `exam_report`(`id`),
  FOREIGN KEY (`compare_report_id`) REFERENCES `exam_report`(`id`)
);

CREATE TABLE `report_compare_result` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `task_id` BIGINT NOT NULL COMMENT '对比任务ID',
  `item_code` VARCHAR(32) NOT NULL COMMENT '项目编码',
  `item_name` VARCHAR(128) NOT NULL COMMENT '项目名称',
  `base_value` VARCHAR(256) COMMENT '基线值',
  `compare_value` VARCHAR(256) COMMENT '对比值',
  `change_value` DECIMAL(18,4) COMMENT '变化值',
  `change_rate` DECIMAL(10,4) COMMENT '变化率',
  `trend` TINYINT DEFAULT 0 COMMENT '趋势：0持平 1上升 2下降',
  FOREIGN KEY (`task_id`) REFERENCES `report_compare_task`(`id`)
);

CREATE TABLE `health_risk_score` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `task_id` BIGINT NOT NULL UNIQUE COMMENT '对比任务ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `score_total` DECIMAL(10,2) DEFAULT 0 COMMENT '总分',
  `score_abnormal` DECIMAL(10,2) DEFAULT 0 COMMENT '异常得分',
  `score_trend` DECIMAL(10,2) DEFAULT 0 COMMENT '趋势得分',
  `risk_level` TINYINT DEFAULT 0 COMMENT '风险等级',
  FOREIGN KEY (`task_id`) REFERENCES `report_compare_task`(`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
);

CREATE TABLE `health_advice_record` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `advice_no` VARCHAR(32) NOT NULL UNIQUE COMMENT '建议单号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `report_id` BIGINT COMMENT '报告ID',
  `compare_task_id` BIGINT COMMENT '对比任务ID',
  `advice_type` VARCHAR(32) NOT NULL COMMENT '建议类型',
  `risk_level` TINYINT DEFAULT 0 COMMENT '风险等级',
  `advice_title` VARCHAR(128) NOT NULL COMMENT '建议标题',
  `advice_content` TEXT NOT NULL COMMENT '建议内容',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`report_id`) REFERENCES `exam_report`(`id`),
  FOREIGN KEY (`compare_task_id`) REFERENCES `report_compare_task`(`id`)
);

-- ============================================
-- 完成！可直接导入SQL转ER图工具
-- ============================================
