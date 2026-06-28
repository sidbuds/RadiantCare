USE `xixin_health`;

-- Users
INSERT INTO `user`
(`id`, `user_no`, `name`, `gender`, `birth_date`, `id_type`, `id_no`, `mobile`, `email`, `password_hash`, `address`, `emergency_contact`, `emergency_mobile`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
(1, 'user', 'Demo User', 1, '1995-01-01', 1, '110101199501010011', '13800000001', 'user@xixin.local', '$2a$10$SDfefLnWWa/RyiXc9ACtqeCDU1/JhkxIPVSrcPuOe9TIMGaqyBUfG', 'Shanghai Pudong', 'Zhang San', '13800001001', 1, NOW(3), NOW(3), 0, 0, 0),
(2, 'doctor-user', 'Demo Doctor', 1, '1988-01-01', 1, '110101198801010022', '13800000002', 'doctor@xixin.local', NULL, 'Shanghai Xuhui', 'Li Si', '13800001002', 1, NOW(3), NOW(3), 0, 0, 0),
(3, 'admin-user', 'Demo Admin', 1, '1985-01-01', 1, '110101198501010033', '13800000003', 'admin@xixin.local', NULL, 'Shanghai Jingan', 'Wang Wu', '13800001003', 1, NOW(3), NOW(3), 0, 0, 0),
(4, 'operator-user', 'Demo Operator', 1, '1990-01-01', 1, '110101199001010044', '13800000004', 'operator@xixin.local', NULL, 'Shanghai Minhang', 'Zhao Liu', '13800001004', 1, NOW(3), NOW(3), 0, 0, 0),
(5, 'doctor2-user', 'Demo Doctor Two', 2, '1989-02-02', 1, '110101198902020055', '13800000005', 'doctor2@xixin.local', NULL, 'Shanghai Huangpu', 'Sun Qi', '13800001005', 1, NOW(3), NOW(3), 0, 0, 0),
(6, 'doctor3-user', 'Demo Doctor Three', 1, '1987-03-03', 1, '110101198703030066', '13800000006', 'doctor3@xixin.local', NULL, 'Shanghai Changning', 'Zhou Ba', '13800001006', 1, NOW(3), NOW(3), 0, 0, 0);

-- Staff accounts
INSERT INTO `staff_account`
(`id`, `username`, `password_hash`, `display_name`, `bind_user_id`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
(1, 'doctor', '$2a$10$SDfefLnWWa/RyiXc9ACtqeCDU1/JhkxIPVSrcPuOe9TIMGaqyBUfG', 'Demo Doctor', 2, 1, NOW(3), NOW(3), 0, 0, 0),
(2, 'admin', '$2a$10$SDfefLnWWa/RyiXc9ACtqeCDU1/JhkxIPVSrcPuOe9TIMGaqyBUfG', 'Demo Admin', 3, 1, NOW(3), NOW(3), 0, 0, 0),
(3, 'operator', '$2a$10$SDfefLnWWa/RyiXc9ACtqeCDU1/JhkxIPVSrcPuOe9TIMGaqyBUfG', 'Demo Operator', 4, 1, NOW(3), NOW(3), 0, 0, 0),
(4, 'doctor2', '$2a$10$SDfefLnWWa/RyiXc9ACtqeCDU1/JhkxIPVSrcPuOe9TIMGaqyBUfG', 'Demo Doctor Two', 5, 1, NOW(3), NOW(3), 0, 0, 0),
(5, 'doctor3', '$2a$10$SDfefLnWWa/RyiXc9ACtqeCDU1/JhkxIPVSrcPuOe9TIMGaqyBUfG', 'Demo Doctor Three', 6, 1, NOW(3), NOW(3), 0, 0, 0);

-- Staff roles
INSERT INTO `staff_role_rel`
(`staff_account_id`, `role_code`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
(1, 'DOCTOR', NOW(3), NOW(3), 0, 0, 0),
(2, 'ADMIN', NOW(3), NOW(3), 0, 0, 0),
(3, 'OPERATOR', NOW(3), NOW(3), 0, 0, 0),
(4, 'DOCTOR', NOW(3), NOW(3), 0, 0, 0),
(5, 'DOCTOR', NOW(3), NOW(3), 0, 0, 0);

-- Health profile
INSERT INTO `health_profile`
(`user_id`, `allergy_history`, `past_medical_history`, `family_history`, `medication_history`, `smoking_status`, `drinking_status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
(1, 'None', 'None', 'None', 'None', 0, 0, NOW(3), NOW(3), 0, 0, 0);

-- Exam centers
INSERT INTO `exam_center`
(`id`, `center_code`, `center_name`, `address`, `phone`, `business_hours`, `description`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
(1, 'C001', 'Xixin Health Pudong Center', '123 Zhangjiang Rd, Pudong, Shanghai', '021-50001001', 'Mon-Sat 07:30-11:30', 'Xixin Health Pudong checkup center with advanced equipment and professional medical team.', 1, NOW(3), NOW(3), 0, 0, 0),
(2, 'C002', 'Xixin Health Xuhui Center', '456 Caoxi North Rd, Xuhui, Shanghai', '021-50001002', 'Mon-Fri 08:00-12:00', 'Xixin Health Xuhui checkup center, convenient location, comfortable environment.', 1, NOW(3), NOW(3), 0, 0, 0);

-- Exam packages
INSERT INTO `exam_package`
(`id`, `package_code`, `package_name`, `category`, `price`, `status`, `remark`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
(1001, 'PKG1001', 'Employment Advanced Package', 'Employment Checkup', 699.00, 1, 'Demo package for end-to-end flow', NOW(3), NOW(3), 0, 0, 0);

-- Exam package items (realistic Chinese health checkup items)
INSERT INTO `exam_package_item`
(`id`, `package_id`, `item_code`, `item_name`, `unit`, `ref_range`, `sort_no`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
(2001, 1001, 'GLU', '空腹血糖', 'mmol/L', '3.9-6.1', 1, NOW(3), NOW(3), 0, 0, 0),
(2002, 1001, 'ALT', '谷丙转氨酶', 'U/L', '0-40', 2, NOW(3), NOW(3), 0, 0, 0),
(2003, 1001, 'AST', '谷草转氨酶', 'U/L', '0-40', 3, NOW(3), NOW(3), 0, 0, 0),
(2004, 1001, 'TC', '总胆固醇', 'mmol/L', '2.8-5.17', 4, NOW(3), NOW(3), 0, 0, 0),
(2005, 1001, 'TG', '甘油三酯', 'mmol/L', '0.56-1.7', 5, NOW(3), NOW(3), 0, 0, 0),
(2006, 1001, 'HDL', '高密度脂蛋白', 'mmol/L', '1.04-1.55', 6, NOW(3), NOW(3), 0, 0, 0),
(2007, 1001, 'LDL', '低密度脂蛋白', 'mmol/L', '<3.37', 7, NOW(3), NOW(3), 0, 0, 0),
(2008, 1001, 'BUN', '尿素氮', 'mmol/L', '2.9-8.2', 8, NOW(3), NOW(3), 0, 0, 0),
(2009, 1001, 'CR', '肌酐', 'umol/L', '44-133', 9, NOW(3), NOW(3), 0, 0, 0),
(2010, 1001, 'UA', '尿酸', 'umol/L', '150-416', 10, NOW(3), NOW(3), 0, 0, 0),
(2011, 1001, 'WBC', '白细胞计数', '10^9/L', '4.0-10.0', 11, NOW(3), NOW(3), 0, 0, 0),
(2012, 1001, 'RBC', '红细胞计数', '10^12/L', '3.5-5.5', 12, NOW(3), NOW(3), 0, 0, 0),
(2013, 1001, 'HGB', '血红蛋白', 'g/L', '110-160', 13, NOW(3), NOW(3), 0, 0, 0),
(2014, 1001, 'PLT', '血小板计数', '10^9/L', '100-300', 14, NOW(3), NOW(3), 0, 0, 0),
(2015, 1001, 'TSH', '促甲状腺激素', 'mIU/L', '0.27-4.2', 15, NOW(3), NOW(3), 0, 0, 0),
(2016, 1001, 'FT4', '游离甲状腺素', 'pmol/L', '12.0-22.0', 16, NOW(3), NOW(3), 0, 0, 0),
(2017, 1001, 'CEA', '癌胚抗原', 'ng/mL', '0-5.0', 17, NOW(3), NOW(3), 0, 0, 0),
(2018, 1001, 'AFP', '甲胎蛋白', 'ng/mL', '0-7.0', 18, NOW(3), NOW(3), 0, 0, 0),
(2019, 1001, 'HBSAG', '乙肝表面抗原', '-', '阴性', 19, NOW(3), NOW(3), 0, 0, 0),
(2020, 1001, 'ECG', '心电图', '-', '-', 20, NOW(3), NOW(3), 0, 0, 0),
(2021, 1001, 'CXR', '胸部X光', '-', '-', 21, NOW(3), NOW(3), 0, 0, 0),
(2022, 1001, 'BU', '腹部B超', '-', '-', 22, NOW(3), NOW(3), 0, 0, 0);

-- Resource capacity
INSERT INTO `resource_capacity`
(`id`, `center_code`, `appoint_date`, `time_slot_code`, `resource_type`, `resource_code`, `capacity_total`, `capacity_used`, `capacity_locked`, `status`, `version_no`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
(3001, 'C001', CURDATE(), 'AM01', 'CENTER_SLOT', 'C001-AM01-D0', 20, 0, 0, 1, 1, NOW(3), NOW(3), 0, 0, 0),
(3002, 'C001', DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'AM01', 'CENTER_SLOT', 'C001-AM01-D1', 20, 0, 0, 1, 1, NOW(3), NOW(3), 0, 0, 0);

-- Exam department routes
INSERT INTO `exam_department_route`
(`id`, `route_code`, `center_code`, `package_id`, `item_code`, `department_code`, `department_name`, `room_no`, `floor_no`, `building_no`, `route_sort`, `guide_text`, `need_empty_stomach`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
(4001, 'ROUTE1001', 'C001', 1001, 'GLU', 'LAB', '检验科', '2F-201', '2F', 'A', 1, '请先到检验科抽血，抽血前需保持空腹。', 1, 1, NOW(3), NOW(3), 0, 0, 0),
(4002, 'ROUTE1002', 'C001', 1001, 'ALT', 'LAB', '检验科', '2F-201', '2F', 'A', 2, '请到检验科进行肝功能检查。', 1, 1, NOW(3), NOW(3), 0, 0, 0),
(4003, 'ROUTE1003', 'C001', 1001, 'ECG', 'CARDIO', '心电图室', '3F-301', '3F', 'A', 3, '请到三楼心电图室进行心电图检查。', 0, 1, NOW(3), NOW(3), 0, 0, 0),
(4004, 'ROUTE1004', 'C001', 1001, 'CXR', 'IMAGING', '影像科', '1F-101', '1F', 'B', 4, '请到一楼影像科进行胸部X光检查。', 0, 1, NOW(3), NOW(3), 0, 0, 0),
(4005, 'ROUTE1005', 'C001', 1001, 'BU', 'ULTRASOUND', '超声科', '2F-205', '2F', 'A', 5, '请到超声科进行腹部B超检查。', 0, 1, NOW(3), NOW(3), 0, 0, 0),
(4006, 'ROUTE1006', 'C001', 1001, 'TSH', 'LAB', '检验科', '2F-201', '2F', 'A', 6, '请到检验科进行甲状腺功能检查。', 0, 1, NOW(3), NOW(3), 0, 0, 0);

-- Report templates
INSERT INTO `report_template`
(`id`, `template_code`, `template_name`, `package_id`, `template_type`, `version_no`, `render_engine`, `template_config`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
(5001, 'PKG1001_V1', 'Employment Package Structured Report V1', 1001, 'STRUCTURED', 1, 'DEFAULT', '{"template":"basic"}', 1, NOW(3), NOW(3), 0, 0, 0);

-- Report section templates
INSERT INTO `report_section_template`
(`id`, `template_id`, `section_code`, `section_name`, `section_type`, `data_source_type`, `item_codes`, `sort_no`, `render_rule`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
(6001, 5001, 'SUMMARY', '综合总结', 'TEXT', 'RESULT', 'GLU,ALT,AST,TC,TG', 1, '{"mode":"summary"}', NOW(3), NOW(3), 0, 0, 0),
(6002, 5001, 'ITEMS', '检查指标', 'TABLE', 'RESULT', 'GLU,ALT,AST,TC,TG,HDL,LDL,BUN,CR,UA,WBC,RBC,HGB,PLT', 2, '{"mode":"table"}', NOW(3), NOW(3), 0, 0, 0);

-- ==================== 阶段一：医生科室关联演示数据 ====================
-- doctor 账号绑定科室（假设 doctor 账号 id=3，请根据实际数据调整）
INSERT INTO `doctor_department_rel` (`doctor_id`, `department_code`, `department_name`, `center_code`, `is_primary`)
VALUES
(3, 'LAB', '检验科', 'C001', 1),
(3, 'IMAGING', '影像科', 'C001', 0);
