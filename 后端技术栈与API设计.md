# 熙心健康体检平台后端技术栈与 API 设计

## 1. 当前后端技术栈
- 语言：`Java 8`
- 框架：`Spring Boot 2.7.x`
- Web：`Spring MVC`
- 安全：`Spring Security + JWT`
- 密码：`BCrypt`
- ORM：`MyBatis-Plus`
- 分页：`PageHelper`
- 数据库：`MySQL 8`
- 运行方式：`Docker MySQL + 本地 Java`，也支持整体 Docker 化

## 2. 当前已落地模块
- 认证登录
- 用户预约
- 用户订单与模拟支付
- 退款申请与运营审核
- 体检任务生成与导检
- 医生录入结果
- 报告生成、审核、发布
- 咨询医生
- 双报告对比与健康建议
- 运营端闭环第一阶段
- 医生异常分析与工作量统计第一阶段

## 3. 角色与认证设计
- 游客角色：未登录访问者，仅允许访问未来开放的公开接口
- 用户角色：`USER`
- 医生角色：`DOCTOR`
- 运营角色：`OPERATOR`
- 管理员角色：`ADMIN`

### 3.1 认证来源
- `USER`：从 `user` 表登录，使用 `user_no + password_hash`
- `DOCTOR / OPERATOR / ADMIN`：从 `staff_account + staff_role_rel` 登录

### 3.2 JWT 载荷
- `accountId`
- `userId`
- `username`
- `displayName`
- `role`

## 4. 主要 API 分组

### 4.0 游客公开接口（文档设计，当前未实现）
- `GET /api/public/packages`
- `GET /api/public/packages/{packageCode}`
- `GET /api/public/centers`
- `GET /api/public/centers/{centerCode}/slots`
- `GET /api/public/content/checkup-guide`
- `GET /api/public/content/faq`

### 4.1 认证
- `POST /api/auth/login`
- `GET /api/auth/me`

### 4.2 用户端
- `GET /api/appointments/available-slots`
- `POST /api/appointments`
- `GET /api/appointments/{appointmentNo}`
- `POST /api/appointments/{appointmentNo}/cancel`
- `POST /api/orders`
- `GET /api/orders/{orderNo}`
- `POST /api/orders/{orderNo}/pay`
- `POST /api/orders/{orderNo}/refund`
- `GET /api/reports/{reportNo}`
- `POST /api/doctor-consultations`
- `POST /api/report-compare/tasks`

### 4.3 医生端
- `GET /api/doctor/exam-tasks/todo`
- `GET /api/doctor/exam-tasks/{taskNo}/items/{taskItemNo}`
- `POST /api/doctor/exam-tasks/{taskNo}/items/{taskItemNo}/start`
- `POST /api/doctor/exam-tasks/{taskNo}/items/{taskItemNo}/results`
- `POST /api/doctor/exam-tasks/{taskNo}/items/{taskItemNo}/complete`
- `GET /api/doctor/consultations/todo`
- `GET /api/doctor/consultations/{consultationNo}`
- `POST /api/doctor/consultations/{consultationNo}/reply`
- `GET /api/doctor/analytics/abnormal-overview`
- `GET /api/doctor/analytics/abnormal-distribution`
- `GET /api/doctor/analytics/high-risk-users`
- `GET /api/doctor/analytics/abnormal-trend`
- `GET /api/doctor/analytics/workload-overview`
- `GET /api/doctor/analytics/workload-trend`
- `GET /api/doctor/analytics/workload-breakdown`

### 4.4 运营端
- `GET /api/operator/packages`
- `POST /api/operator/packages`
- `PUT /api/operator/packages/{id}`
- `GET /api/operator/schedules`
- `POST /api/operator/schedules`
- `PUT /api/operator/schedules/{id}`
- `GET /api/operator/appointments`
- `GET /api/operator/orders`
- `GET /api/operator/orders/export`
- `GET /api/operator/refunds`
- `GET /api/operator/refunds/{applyNo}`
- `POST /api/operator/refunds/{applyNo}/approve`
- `POST /api/operator/refunds/{applyNo}/reject`
- `GET /api/operator/analytics/dashboard`
- `GET /api/operator/analytics/appointment-trend`
- `GET /api/operator/analytics/order-conversion`
- `GET /api/operator/analytics/package-analysis`

### 4.5 管理端
- `POST /api/admin/exam-tasks/generate`
- `POST /api/admin/reports/generate`
- `POST /api/admin/reports/{reportNo}/review`
- `POST /api/admin/reports/{reportNo}/publish`
- `POST /api/admin/doctor-consultations/{consultationNo}/assign`
- `GET /api/admin/doctor-analytics/abnormal-overview`
- `GET /api/admin/doctor-analytics/abnormal-distribution`
- `GET /api/admin/doctor-analytics/high-risk-users`
- `GET /api/admin/doctor-analytics/workload-ranking`
- `GET /api/admin/doctor-analytics/department-workload`

## 5. 医生异常分析与工作量统计口径

### 5.1 异常分析
- 基于 `exam_result`
- 主维度为录入医生、科室、日期
- 高风险口径：`is_abnormal = 1` 且 `abnormal_level >= 2`
- 首期输出总量、异常量、异常率、高风险量、项目分布、趋势、高风险用户清单

### 5.2 工作量统计
- 检查项完成数：基于 `exam_task_item.item_status = 2`
- 结果录入数：基于 `exam_result.entry_doctor_id`
- 报告审核数：基于 `doctor_review_record.reviewer_id`
- 咨询回复数：基于 `doctor_consultation_reply.reply_role = DOCTOR`
- 平均处理时长：基于 `exam_task_item.start_time` 与 `complete_time`

### 5.3 权限边界
- 医生只能看自己的分析数据
- 管理员可按时间、医生、科室查看汇总数据
- 运营端不直接查看医生分析

## 6. 当前实现边界
- 游客后续统一通过 `/api/public/**` 访问公开能力，但这组接口当前尚未实现
- 创建预约、订单、咨询、报告查看、对比等仍需登录
- 运营导出先返回结构化 JSON，不生成文件
- 退款仍为整单退款 + 同步 mock 成功
- 报告对比仍只支持两份已发布报告
- 咨询仍仅支持人工医生回复
- 医生分析首期采用实时聚合，不引入离线统计表
