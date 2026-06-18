# 熙心健康体检平台前端技术栈、API 文档与界面设计

## 1. 前端技术栈

### 1.1 推荐方案
| 层级 | 方案 |
|---|---|
| 语言 | TypeScript |
| 框架 | Vue 3 + Vite |
| UI 组件库 | Element Plus |
| 状态管理 | Pinia |
| 路由 | Vue Router |
| 请求 | Axios |
| 图表 | ECharts |
| 样式 | SCSS / UnoCSS |
| 单测 | Vitest |
| E2E | Playwright |

### 1.2 模块划分
- 游客端：首页、套餐浏览、中心介绍、流程说明、常见问题、登录转化
- 用户端：预约、订单、导检、报告、对比、咨询医生
- 医生端：待检任务、结果录入、报告审核、咨询回复、异常分析、工作量统计
- 运营端：套餐、排班、预约、订单、退款、经营分析
- 管理端：任务生成、报告发布、咨询分配、医生分析总览

## 2. API 前缀
| 模块 | 前缀 |
|---|---|
| 游客公开接口 | `/api/public` |
| 认证 | `/api/auth` |
| 预约 | `/api/appointments` |
| 订单 | `/api/orders` |
| 体检任务 | `/api/exam-tasks` |
| 报告 | `/api/reports` |
| 历年对比 | `/api/report-compare` |
| 健康建议 | `/api/health-advices` |
| 咨询医生 | `/api/doctor-consultations` |
| 医生端 | `/api/doctor/**` |
| 运营端 | `/api/operator/**` |
| 管理端 | `/api/admin/**` |

## 3. 页面设计

### 3.0 游客端
- 首页
- 套餐列表页
- 套餐详情页
- 中心介绍页
- 体检流程说明页
- 常见问题页
- 登录/注册引导弹窗

### 3.1 用户端
- 登录页
- 套餐列表页
- 预约页
- 订单确认页
- 我的预约页
- 我的体检任务页
- 导检路线页
- 报告列表页
- 报告详情页
- 历年对比页
- 咨询医生页

### 3.2 医生端
- 待检任务列表页
- 检查项详情页
- 结果录入页
- 报告审核页
- 咨询回复页
- 异常分析页
- 工作量统计页

### 3.3 运营端
- 套餐管理页
- 排班管理页
- 预约管理页
- 订单管理页
- 退款审核页
- 运营看板页

### 3.4 管理端
- 体检任务生成页
- 报告发布管理页
- 咨询分配页
- 医生分析总览页
- 医生工作量排行页

## 4. 核心交互设计

### 4.1 用户下单到报告
- 游客在首页、套餐页和中心页可先浏览公开信息，不要求先登录。
- 游客点击“立即预约”“立即下单”“咨询医生”时，前端弹出登录/注册引导。
- 用户完成登录后，前端尽量回跳原页面并保留已选套餐、中心、日期等上下文。
- 用户在套餐详情页选择中心、日期、时段后创建预约
- 订单确认页展示套餐检查项，如血检、呼吸道体检、影像、超声等
- 到院后“我的体检任务”展示导检路线、当前进度、待检项目
- 报告详情页展示总体结论、指标明细、异常项、下载与分享入口

### 4.2 医生录入到发布
- 医生待检页按科室、状态筛选任务项
- 结果录入页分为结构化指标、结论备注、附件区
- 审核页展示报告章节、异常汇总、审核意见
- 咨询回复页展示用户问题、关联报告、历史回复

### 4.3 医生分析工作台
- 异常分析页展示：
  - 异常总数
  - 异常率
  - 高风险数
  - 异常项目分布
  - 异常趋势图
  - 高风险用户清单
- 工作量统计页展示：
  - 完成检查项数
  - 结果录入数
  - 报告审核数
  - 咨询回复数
  - 平均处理时长
  - 按项目/科室拆分结果

### 4.4 管理员医生分析总览
- 支持按日期、医生、科室筛选
- 展示医生异常总览、高风险用户、工作量排行、科室工作量汇总
- 与运营看板分离，不混用入口

## 5. 前端接口清单

### 5.1 用户端
- `GET /api/exam-tasks/mine/current`
- `GET /api/exam-tasks/{taskNo}/guide`
- `GET /api/exam-tasks/{taskNo}/items`
- `GET /api/reports/mine`
- `GET /api/reports/{reportNo}`
- `POST /api/doctor-consultations`
- `GET /api/doctor-consultations/mine`
- `POST /api/report-compare/tasks`
- `GET /api/health-advices/compare-tasks/{taskNo}`

### 5.2 医生端
- `GET /api/doctor/exam-tasks/todo`
- `GET /api/doctor/exam-tasks/{taskNo}/items/{taskItemNo}`
- `POST /api/doctor/exam-tasks/{taskNo}/items/{taskItemNo}/results`
- `POST /api/doctor/exam-tasks/{taskNo}/items/{taskItemNo}/complete`
- `GET /api/doctor/consultations/todo`
- `POST /api/doctor/consultations/{consultationNo}/reply`
- `GET /api/doctor/analytics/abnormal-overview`
- `GET /api/doctor/analytics/abnormal-distribution`
- `GET /api/doctor/analytics/high-risk-users`
- `GET /api/doctor/analytics/abnormal-trend`
- `GET /api/doctor/analytics/workload-overview`
- `GET /api/doctor/analytics/workload-trend`
- `GET /api/doctor/analytics/workload-breakdown`

### 5.3 运营端
- `GET /api/operator/packages`
- `POST /api/operator/packages`
- `GET /api/operator/schedules`
- `POST /api/operator/schedules`
- `GET /api/operator/appointments`
- `GET /api/operator/orders`
- `GET /api/operator/refunds`
- `POST /api/operator/refunds/{applyNo}/approve`
- `POST /api/operator/refunds/{applyNo}/reject`
- `GET /api/operator/analytics/dashboard`
- `GET /api/operator/analytics/appointment-trend`
- `GET /api/operator/analytics/order-conversion`
- `GET /api/operator/analytics/package-analysis`

### 5.4 管理端
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

### 5.5 游客公开接口（文档设计，当前未实现）
- `GET /api/public/packages`
- `GET /api/public/packages/{packageCode}`
- `GET /api/public/centers`
- `GET /api/public/centers/{centerCode}/slots`
- `GET /api/public/content/checkup-guide`
- `GET /api/public/content/faq`

## 6. 关键组件建议
- `PackageItemList`
- `ExamGuideTimeline`
- `ExamTaskProgress`
- `DoctorResultForm`
- `ReportSectionRenderer`
- `HealthAdviceCard`
- `DoctorConsultationDrawer`
- `DoctorAbnormalDashboard`
- `DoctorWorkloadDashboard`
- `AdminDoctorAnalyticsPanel`
