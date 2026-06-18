// ============================================
// 熙心健康体检平台 - DBML格式（dbdiagram.io专用）
// 复制到 https://dbdiagram.io/d 即可生成ER图
// ============================================

// ========== 用户体系 ==========
Table user {
  id BIGINT [pk, increment]
  user_no VARCHAR(32) [not null, unique]
  name VARCHAR(64) [not null]
  gender TINYINT
  mobile VARCHAR(20) [not null, unique]
  password_hash VARCHAR(128)
  status TINYINT [default: 1]
}

Table staff_account {
  id BIGINT [pk, increment]
  username VARCHAR(64) [not null, unique]
  password_hash VARCHAR(128) [not null]
  display_name VARCHAR(64) [not null]
  bind_user_id BIGINT [not null]
  status TINYINT [default: 1]
}

Table staff_role_rel {
  id BIGINT [pk, increment]
  staff_account_id BIGINT [not null]
  role_code VARCHAR(32) [not null]
}

// ========== 预约套餐 ==========
Table exam_package {
  id BIGINT [pk, increment]
  package_code VARCHAR(32) [not null, unique]
  package_name VARCHAR(128) [not null]
  category VARCHAR(32)
  price DECIMAL(12,2) [not null, default: 0]
  status TINYINT [default: 1]
}

Table exam_package_item {
  id BIGINT [pk, increment]
  package_id BIGINT [not null]
  item_code VARCHAR(32) [not null]
  item_name VARCHAR(128) [not null]
  unit VARCHAR(32)
  ref_range VARCHAR(128)
  sort_no INT [default: 0]
}

Table resource_capacity {
  id BIGINT [pk, increment]
  center_code VARCHAR(32) [not null]
  appoint_date DATE [not null]
  time_slot_code VARCHAR(32) [not null]
  capacity_total INT [default: 0]
  capacity_used INT [default: 0]
  status TINYINT [default: 1]
}

Table appointment {
  id BIGINT [pk, increment]
  appointment_no VARCHAR(32) [not null, unique]
  user_id BIGINT [not null]
  package_id BIGINT [not null]
  center_code VARCHAR(32) [not null]
  appoint_date DATE [not null]
  time_slot_code VARCHAR(32) [not null]
  status TINYINT [default: 0]
}

// ========== 订单支付 ==========
Table order {
  id BIGINT [pk, increment]
  order_no VARCHAR(32) [not null, unique]
  appointment_id BIGINT [not null]
  user_id BIGINT [not null]
  package_id BIGINT [not null]
  total_amount DECIMAL(12,2) [default: 0]
  pay_amount DECIMAL(12,2) [default: 0]
  discount_amount DECIMAL(12,2) [default: 0]
  status TINYINT [default: 0]
  pay_channel VARCHAR(32)
}

Table order_item {
  id BIGINT [pk, increment]
  order_id BIGINT [not null]
  item_type TINYINT [default: 0]
  ref_item_code VARCHAR(32) [not null]
  ref_item_name VARCHAR(128) [not null]
  qty INT [default: 1]
  unit_price DECIMAL(12,2) [default: 0]
  amount DECIMAL(12,2) [default: 0]
}

Table payment_record {
  id BIGINT [pk, increment]
  payment_no VARCHAR(32) [not null, unique]
  order_id BIGINT [not null]
  channel VARCHAR(32) [not null]
  trade_no VARCHAR(64)
  pay_amount DECIMAL(12,2) [default: 0]
  status TINYINT [default: 0]
}

// ========== 体检执行 ==========
Table exam_task {
  id BIGINT [pk, increment]
  task_no VARCHAR(32) [not null, unique]
  appointment_id BIGINT [not null]
  order_id BIGINT [not null]
  user_id BIGINT [not null]
  task_date DATE [not null]
  task_status TINYINT [default: 0]
  report_status TINYINT [default: 0]
}

Table exam_department_route {
  id BIGINT [pk, increment]
  route_code VARCHAR(32) [not null, unique]
  center_code VARCHAR(32) [not null]
  package_id BIGINT [not null]
  item_code VARCHAR(32) [not null]
  department_code VARCHAR(32) [not null]
  department_name VARCHAR(128) [not null]
  room_no VARCHAR(64)
  route_sort INT [default: 0]
}

Table exam_task_item {
  id BIGINT [pk, increment]
  task_item_no VARCHAR(32) [not null, unique]
  task_id BIGINT [not null]
  item_code VARCHAR(32) [not null]
  item_name VARCHAR(128) [not null]
  department_code VARCHAR(32) [not null]
  doctor_id BIGINT
  item_status TINYINT [default: 0]
}

Table exam_result {
  id BIGINT [pk, increment]
  result_no VARCHAR(32) [not null, unique]
  task_id BIGINT [not null]
  task_item_id BIGINT [not null]
  user_id BIGINT [not null]
  item_code VARCHAR(32) [not null]
  result_type VARCHAR(32) [not null]
  result_value VARCHAR(256)
  result_number DECIMAL(18,4)
  is_abnormal TINYINT [default: 0]
  abnormal_level TINYINT [default: 0]
  entry_doctor_id BIGINT
}

// ========== 报告管理 ==========
Table report_template {
  id BIGINT [pk, increment]
  template_code VARCHAR(32) [not null, unique]
  template_name VARCHAR(128) [not null]
  package_id BIGINT [not null]
  template_type VARCHAR(32) [not null]
  status TINYINT [default: 1]
}

Table exam_report {
  id BIGINT [pk, increment]
  report_no VARCHAR(32) [not null, unique]
  appointment_id BIGINT [not null]
  task_id BIGINT [not null]
  user_id BIGINT [not null]
  package_id BIGINT [not null]
  template_id BIGINT
  report_date DATE [not null]
  overall_conclusion TEXT
  status TINYINT [default: 0]
  pdf_url VARCHAR(512)
}

Table exam_report_item {
  id BIGINT [pk, increment]
  report_id BIGINT [not null]
  item_code VARCHAR(32) [not null]
  item_name VARCHAR(128) [not null]
  result_value VARCHAR(256)
  result_number DECIMAL(18,4)
  is_abnormal TINYINT [default: 0]
  abnormal_level TINYINT [default: 0]
  sort_no INT [default: 0]
}

Table doctor_review_record {
  id BIGINT [pk, increment]
  review_no VARCHAR(32) [not null, unique]
  report_id BIGINT [not null]
  task_id BIGINT [not null]
  review_stage VARCHAR(32) [not null]
  review_status VARCHAR(32) [not null]
  review_comment VARCHAR(512)
  reviewer_id BIGINT
}

// ========== 咨询管理 ==========
Table doctor_consultation {
  id BIGINT [pk, increment]
  consultation_no VARCHAR(32) [not null, unique]
  user_id BIGINT [not null]
  doctor_id BIGINT
  report_id BIGINT
  source_type VARCHAR(32) [not null]
  consultation_type VARCHAR(32) [not null]
  consultation_title VARCHAR(128)
  consultation_content TEXT [not null]
  consultation_status TINYINT [default: 0]
  priority_level TINYINT [default: 0]
}

Table doctor_consultation_reply {
  id BIGINT [pk, increment]
  consultation_id BIGINT [not null]
  consultation_no VARCHAR(32) [not null]
  reply_role VARCHAR(16) [not null]
  reply_user_id BIGINT
  reply_user_name VARCHAR(64)
  reply_content TEXT [not null]
  attachment_url VARCHAR(512)
  reply_time DATETIME [not null]
}

// ========== 数据分析 ==========
Table report_compare_task {
  id BIGINT [pk, increment]
  task_no VARCHAR(32) [not null, unique]
  user_id BIGINT [not null]
  baseline_report_id BIGINT [not null]
  compare_report_id BIGINT [not null]
  status TINYINT [default: 0]
}

Table report_compare_result {
  id BIGINT [pk, increment]
  task_id BIGINT [not null]
  item_code VARCHAR(32) [not null]
  item_name VARCHAR(128) [not null]
  base_value VARCHAR(256)
  compare_value VARCHAR(256)
  change_value DECIMAL(18,4)
  change_rate DECIMAL(10,4)
  trend TINYINT [default: 0]
}

Table health_risk_score {
  id BIGINT [pk, increment]
  task_id BIGINT [not null, unique]
  user_id BIGINT [not null]
  score_total DECIMAL(10,2) [default: 0]
  score_abnormal DECIMAL(10,2) [default: 0]
  score_trend DECIMAL(10,2) [default: 0]
  risk_level TINYINT [default: 0]
}

Table health_advice_record {
  id BIGINT [pk, increment]
  advice_no VARCHAR(32) [not null, unique]
  user_id BIGINT [not null]
  report_id BIGINT
  compare_task_id BIGINT
  advice_type VARCHAR(32) [not null]
  risk_level TINYINT [default: 0]
  advice_title VARCHAR(128) [not null]
  advice_content TEXT [not null]
}

// ============================================
// 关系定义
// ============================================

// 用户体系
Ref: staff_account.bind_user_id > user.id
Ref: staff_role_rel.staff_account_id > staff_account.id

// 预约套餐
Ref: exam_package_item.package_id > exam_package.id
Ref: appointment.user_id > user.id
Ref: appointment.package_id > exam_package.id

// 订单支付
Ref: order.appointment_id > appointment.id
Ref: order.user_id > user.id
Ref: order_item.order_id > order.id
Ref: payment_record.order_id > order.id

// 体检执行
Ref: exam_task.appointment_id > appointment.id
Ref: exam_task.order_id > order.id
Ref: exam_task.user_id > user.id
Ref: exam_task_item.task_id > exam_task.id
Ref: exam_task_item.doctor_id > staff_account.id
Ref: exam_result.task_id > exam_task.id
Ref: exam_result.task_item_id > exam_task_item.id
Ref: exam_result.user_id > user.id
Ref: exam_result.entry_doctor_id > staff_account.id
Ref: exam_department_route.package_id > exam_package.id

// 报告管理
Ref: report_template.package_id > exam_package.id
Ref: exam_report.appointment_id > appointment.id
Ref: exam_report.task_id > exam_task.id
Ref: exam_report.user_id > user.id
Ref: exam_report.template_id > report_template.id
Ref: exam_report_item.report_id > exam_report.id
Ref: doctor_review_record.report_id > exam_report.id
Ref: doctor_review_record.task_id > exam_task.id
Ref: doctor_review_record.reviewer_id > staff_account.id

// 咨询管理
Ref: doctor_consultation.user_id > user.id
Ref: doctor_consultation.doctor_id > staff_account.id
Ref: doctor_consultation.report_id > exam_report.id
Ref: doctor_consultation_reply.consultation_id > doctor_consultation.id
Ref: doctor_consultation_reply.reply_user_id > user.id

// 数据分析
Ref: report_compare_task.user_id > user.id
Ref: report_compare_task.baseline_report_id > exam_report.id
Ref: report_compare_task.compare_report_id > exam_report.id
Ref: report_compare_result.task_id > report_compare_task.id
Ref: health_risk_score.task_id > report_compare_task.id
Ref: health_risk_score.user_id > user.id
Ref: health_advice_record.user_id > user.id
Ref: health_advice_record.report_id > exam_report.id
Ref: health_advice_record.compare_task_id > report_compare_task.id
