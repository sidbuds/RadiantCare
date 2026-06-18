# 链路-表-API-代码映射文档

## 总览
- 当前后端已落地：认证、预约、订单、退款、体检任务、医生录入、报告、咨询医生、双报告对比、健康建议、运营端第一阶段、医生分析第一阶段
- 当前未落地：游客公开浏览链路、PDF 报告导出、多年度聚合对比、离线统计汇总、完整 RBAC 菜单权限

## 0. 游客公开浏览链路（文档已设计，后端未实现）
- 表：
  - `exam_package`
  - `exam_package_item`
  - `resource_capacity`
- 后续建议 API：
  - `GET /api/public/packages`
  - `GET /api/public/packages/{packageCode}`
  - `GET /api/public/centers`
  - `GET /api/public/centers/{centerCode}/slots`
  - `GET /api/public/content/checkup-guide`
  - `GET /api/public/content/faq`
- 当前状态：
  - 已完成文档设计
  - 当前后端代码未提供 `/api/public/**` 控制器

## 1. 认证登录
- 表：
  - `user`
  - `staff_account`
  - `staff_role_rel`
- API：
  - `POST /api/auth/login`
  - `GET /api/auth/me`
- 代码：
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\auth\controller\AuthController.java:1`
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\auth\service\AuthService.java:1`

## 2. 预约链路
- 表：
  - `exam_package`
  - `exam_package_item`
  - `appointment`
  - `resource_capacity`
- API：
  - `GET /api/appointments/available-slots`
  - `POST /api/appointments`
  - `GET /api/appointments/{appointmentNo}`
  - `GET /api/appointments/mine`
  - `POST /api/appointments/{appointmentNo}/cancel`
- 代码：
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\appointment\controller\AppointmentController.java:1`
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\appointment\service\AppointmentService.java:1`

## 3. 订单与支付链路
- 表：
  - `order`
  - `order_item`
  - `payment_record`
- API：
  - `POST /api/orders`
  - `GET /api/orders/{orderNo}`
  - `GET /api/orders/mine`
  - `POST /api/orders/{orderNo}/pay`
- 代码：
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\order\controller\OrderController.java:1`
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\order\service\OrderService.java:1`

## 4. 退款链路
- 表：
  - `refund_apply`
  - `refund_audit_log`
  - `refund_record`
  - `order`
- API：
  - `POST /api/orders/{orderNo}/refund`
  - `GET /api/operator/refunds`
  - `GET /api/operator/refunds/{applyNo}`
  - `POST /api/operator/refunds/{applyNo}/approve`
  - `POST /api/operator/refunds/{applyNo}/reject`
- 代码：
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\order\controller\OrderController.java:1`
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\operator\controller\OperatorRefundController.java:1`
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\order\service\RefundService.java:1`

## 5. 体检任务与导检链路
- 表：
  - `exam_task`
  - `exam_task_item`
  - `exam_department_route`
- API：
  - `POST /api/admin/exam-tasks/generate`
  - `GET /api/exam-tasks/mine/current`
  - `GET /api/exam-tasks/{taskNo}`
  - `GET /api/exam-tasks/{taskNo}/guide`
  - `GET /api/exam-tasks/{taskNo}/items`
- 代码：
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\exam\controller\AdminExamController.java:1`
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\exam\controller\ExamTaskController.java:1`
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\exam\service\ExamService.java:1`

## 6. 医生录入结果链路
- 表：
  - `exam_result`
  - `exam_task_item`
  - `exam_task`
- API：
  - `GET /api/doctor/exam-tasks/todo`
  - `GET /api/doctor/exam-tasks/{taskNo}/items/{taskItemNo}`
  - `POST /api/doctor/exam-tasks/{taskNo}/items/{taskItemNo}/start`
  - `POST /api/doctor/exam-tasks/{taskNo}/items/{taskItemNo}/results`
  - `POST /api/doctor/exam-tasks/{taskNo}/items/{taskItemNo}/complete`
- 代码：
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\exam\controller\DoctorExamController.java:1`
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\exam\service\ExamService.java:1`

## 7. 报告生成、审核、发布链路
- 表：
  - `report_template`
  - `report_section_template`
  - `exam_report`
  - `exam_report_item`
  - `doctor_review_record`
- API：
  - `POST /api/admin/reports/generate`
  - `POST /api/admin/reports/{reportNo}/review`
  - `POST /api/admin/reports/{reportNo}/publish`
  - `GET /api/reports/mine`
  - `GET /api/reports/{reportNo}`
  - `GET /api/reports/{reportNo}/items`
- 代码：
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\report\controller\AdminReportController.java:1`
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\report\controller\ReportController.java:1`
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\report\service\ReportService.java:1`

## 8. 咨询医生链路
- 表：
  - `doctor_consultation`
  - `doctor_consultation_reply`
  - `exam_report`
- API：
  - `POST /api/doctor-consultations`
  - `GET /api/doctor-consultations/mine`
  - `GET /api/doctor-consultations/{consultationNo}`
  - `POST /api/doctor-consultations/{consultationNo}/close`
  - `GET /api/doctor/consultations/todo`
  - `GET /api/doctor/consultations/{consultationNo}`
  - `POST /api/doctor/consultations/{consultationNo}/reply`
  - `POST /api/admin/doctor-consultations/{consultationNo}/assign`
- 代码：
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\consultation\controller\ConsultationController.java:1`
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\consultation\controller\DoctorConsultationController.java:1`
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\consultation\controller\AdminConsultationController.java:1`
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\consultation\service\ConsultationService.java:1`

## 9. 双报告对比链路
- 表：
  - `report_compare_task`
  - `report_compare_result`
  - `indicator_trend_tag`
  - `health_risk_score`
  - `exam_report`
  - `exam_report_item`
- API：
  - `POST /api/report-compare/tasks`
  - `GET /api/report-compare/tasks/{taskNo}`
  - `GET /api/report-compare/tasks/{taskNo}/results`
  - `GET /api/report-compare/tasks/{taskNo}/export`
- 代码：
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\compare\controller\ReportCompareController.java:1`
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\compare\service\CompareService.java:1`

## 10. 健康建议链路
- 表：
  - `health_advice_record`
  - `health_risk_score`
  - `report_compare_result`
- API：
  - `GET /api/health-advices/compare-tasks/{taskNo}`
- 代码：
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\compare\controller\HealthAdviceController.java:1`
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\compare\service\CompareService.java:1`

## 11. 医生异常分析与工作量统计链路
- 表：
  - `exam_task_item`
  - `exam_result`
  - `doctor_review_record`
  - `doctor_consultation_reply`
  - `user`
- API：
  - `GET /api/doctor/analytics/abnormal-overview`
  - `GET /api/doctor/analytics/abnormal-distribution`
  - `GET /api/doctor/analytics/high-risk-users`
  - `GET /api/doctor/analytics/abnormal-trend`
  - `GET /api/doctor/analytics/workload-overview`
  - `GET /api/doctor/analytics/workload-trend`
  - `GET /api/doctor/analytics/workload-breakdown`
  - `GET /api/admin/doctor-analytics/abnormal-overview`
  - `GET /api/admin/doctor-analytics/abnormal-distribution`
  - `GET /api/admin/doctor-analytics/high-risk-users`
  - `GET /api/admin/doctor-analytics/workload-ranking`
  - `GET /api/admin/doctor-analytics/department-workload`
- 代码：
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\doctor\controller\DoctorAnalyticsController.java:1`
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\doctor\controller\AdminDoctorAnalyticsController.java:1`
  - `D:\projects\熙心健康体检平台\backend\src\main\java\com\xixin\health\doctor\service\DoctorAnalyticsService.java:1`

## 12. 当前实现边界
- 游客能力当前仅完成文档设计，未真正开放后端接口
- 运营人员已使用独立 `OPERATOR` 角色，不再复用 `ADMIN`
- 报告对比仅支持两份已发布报告，且仅处理数值型指标
- 健康建议为规则式建议，不包含 AI 生成
- 导出接口仅返回结构化 JSON，不生成文件
- 医生分析首期仅做实时聚合，不做离线汇总
