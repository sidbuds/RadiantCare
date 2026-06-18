USE `xixin_health`;

-- Users
INSERT INTO `user`
(`id`, `user_no`, `name`, `gender`, `birth_date`, `id_type`, `id_no`, `mobile`, `email`, `password_hash`, `address`, `emergency_contact`, `emergency_mobile`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
(1, 'user', 'Demo User', 1, '1995-01-01', 1, '110101199501010011', '13800000001', 'user@xixin.local', '$2a$10$SDfefLnWWa/RyiXc9ACtqeCDU1/JhkxIPVSrcPuOe9TIMGaqyBUfG', 'Shanghai Pudong', 'Zhang San', '13800001001', 1, NOW(3), NOW(3), 0, 0, 0),
(2, 'doctor-user', 'Demo Doctor', 1, '1988-01-01', 1, '110101198801010022', '13800000002', 'doctor@xixin.local', NULL, 'Shanghai Xuhui', 'Li Si', '13800001002', 1, NOW(3), NOW(3), 0, 0, 0),
(3, 'admin-user', 'Demo Admin', 1, '1985-01-01', 1, '110101198501010033', '13800000003', 'admin@xixin.local', NULL, 'Shanghai Jingan', 'Wang Wu', '13800001003', 1, NOW(3), NOW(3), 0, 0, 0),
(4, 'operator-user', 'Demo Operator', 1, '1990-01-01', 1, '110101199001010044', '13800000004', 'operator@xixin.local', NULL, 'Shanghai Minhang', 'Zhao Liu', '13800001004', 1, NOW(3), NOW(3), 0, 0, 0);

-- Staff accounts
INSERT INTO `staff_account`
(`id`, `username`, `password_hash`, `display_name`, `bind_user_id`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
(1, 'doctor', '$2a$10$SDfefLnWWa/RyiXc9ACtqeCDU1/JhkxIPVSrcPuOe9TIMGaqyBUfG', 'Demo Doctor', 2, 1, NOW(3), NOW(3), 0, 0, 0),
(2, 'admin', '$2a$10$SDfefLnWWa/RyiXc9ACtqeCDU1/JhkxIPVSrcPuOe9TIMGaqyBUfG', 'Demo Admin', 3, 1, NOW(3), NOW(3), 0, 0, 0),
(3, 'operator', '$2a$10$SDfefLnWWa/RyiXc9ACtqeCDU1/JhkxIPVSrcPuOe9TIMGaqyBUfG', 'Demo Operator', 4, 1, NOW(3), NOW(3), 0, 0, 0);

-- Staff roles
INSERT INTO `staff_role_rel`
(`staff_account_id`, `role_code`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
(1, 'DOCTOR', NOW(3), NOW(3), 0, 0, 0),
(2, 'ADMIN', NOW(3), NOW(3), 0, 0, 0),
(3, 'OPERATOR', NOW(3), NOW(3), 0, 0, 0);

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

-- Exam package items
INSERT INTO `exam_package_item`
(`id`, `package_id`, `item_code`, `item_name`, `unit`, `ref_range`, `sort_no`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
(2001, 1001, 'BLOOD', 'Blood Test', NULL, NULL, 1, NOW(3), NOW(3), 0, 0, 0),
(2002, 1001, 'RESP', 'Respiratory Exam', NULL, NULL, 2, NOW(3), NOW(3), 0, 0, 0);

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
(4001, 'ROUTE1001', 'C001', 1001, 'BLOOD', 'LAB', 'Laboratory', '2F-201', '2F', 'A', 1, 'Go to the laboratory first and keep fasting before blood draw.', 1, 1, NOW(3), NOW(3), 0, 0, 0),
(4002, 'ROUTE1002', 'C001', 1001, 'RESP', 'RESP', 'Respiratory Clinic', '3F-305', '3F', 'A', 2, 'After blood test, go to the respiratory clinic for follow-up exam.', 0, 1, NOW(3), NOW(3), 0, 0, 0);

-- Report templates
INSERT INTO `report_template`
(`id`, `template_code`, `template_name`, `package_id`, `template_type`, `version_no`, `render_engine`, `template_config`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
(5001, 'PKG1001_V1', 'Employment Package Structured Report V1', 1001, 'STRUCTURED', 1, 'DEFAULT', '{"template":"basic"}', 1, NOW(3), NOW(3), 0, 0, 0);

-- Report section templates
INSERT INTO `report_section_template`
(`id`, `template_id`, `section_code`, `section_name`, `section_type`, `data_source_type`, `item_codes`, `sort_no`, `render_rule`, `created_at`, `updated_at`, `created_by`, `updated_by`, `is_deleted`)
VALUES
(6001, 5001, 'SUMMARY', 'Overall Summary', 'TEXT', 'RESULT', 'GLU,RESP_DESC', 1, '{"mode":"summary"}', NOW(3), NOW(3), 0, 0, 0),
(6002, 5001, 'ITEMS', 'Indicators', 'TABLE', 'RESULT', 'GLU,RESP_DESC', 2, '{"mode":"table"}', NOW(3), NOW(3), 0, 0, 0);
