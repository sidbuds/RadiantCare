# 熙心健康体检平台 — 项目总览

## 1. 项目定位

面向体检机构的全链路数字化平台，覆盖 **预约 → 到检 → 检查 → 报告 → 咨询 → 对比** 完整闭环。支持四类角色：用户、医生、运营、管理员。

## 2. 核心业务链路

```
访客浏览套餐 → 用户注册登录 → 选择套餐/中心/时段 → 创建预约 → 生成订单 → 支付
    → 管理员生成体检任务 → 用户到检（导检路线）→ 医生逐项检查并录入结果
    → 管理员生成报告 → 医生审核 → 发布报告 → 用户查看/历年对比
    → 发起在线咨询 → 医生回复 → 健康建议
```

## 3. 技术架构

```
┌─────────────────────────────────────────────┐
│                  前端 (Vue 3)                │
│  Vite + TypeScript + Element Plus + ECharts  │
│  Pinia 状态管理 · Vue Router 路由守卫        │
└──────────────────┬──────────────────────────┘
                   │ HTTP (Axios, /api 代理)
┌──────────────────▼──────────────────────────┐
│              后端 (Spring Boot 2.7)          │
│  Spring Security + JWT 认证                  │
│  MyBatis-Plus ORM · PageHelper 分页          │
└──────────────────┬──────────────────────────┘
                   │ JDBC
┌──────────────────▼──────────────────────────┐
│              MySQL 8                         │
│  xixin_health 数据库 · 52 张表               │
└─────────────────────────────────────────────┘
```

## 4. 数据库核心表（52 张）

| 模块 | 核心表 |
|---|---|
| 认证 | account, user_info |
| 套餐 | exam_package, package_template, package_template_item |
| 中心 | exam_center |
| 排班 | center_schedule |
| 预约 | appointment |
| 订单 | order_info, refund_apply |
| 检查 | exam_task, exam_task_item, exam_result |
| 报告 | report, report_template, report_review_record |
| 对比 | compare_task, compare_result, health_advice |
| 咨询 | consultation, consultation_reply |

**设计规范**：BIGINT 主键 · DATETIME(3) 时间 · 逻辑删除 · 无物理外键 · 统一审计字段

## 5. API 模块（10 组）

| 模块 | 端点数 | 说明 |
|---|---|---|
| auth | 2 | 登录 + 获取用户信息 |
| public | 5 | 套餐/中心/流程/FAQ（无需登录） |
| appointment | 5 | 预约 CRUD |
| order | 4 | 订单创建/查询/支付/退款 |
| exam | 6 | 体检任务/结果录入/完成 |
| report | 4 | 报告生成/审核/发布 |
| compare | 4 | 历年对比任务/结果/健康建议 |
| consultation | 5 | 咨询创建/查询/回复/分配 |
| operator | 15+ | 运营端套餐/排班/预约/订单/退款/分析 |
| doctor-analytics | 8 | 异常分析/工作量统计 |

## 6. 前端页面（35 页）

| 角色 | 页面数 | 主要页面 |
|---|---|---|
| 访客 | 6 | 首页、套餐列表/详情、体检中心、检查流程、FAQ |
| 用户 | 9 | 预约管理/创建/详情、订单确认、导检路线、报告列表/详情/对比、咨询 |
| 医生 | 5 | 待检任务、结果录入、咨询回复、异常分析、工作量统计 |
| 运营 | 6 | 套餐管理、排班管理、预约/订单/退款管理、运营看板 |
| 管理 | 4 | 任务生成、报告发布、咨询分配、医生分析 |

## 7. 设计系统

**配色**：深炭灰底 `#111113` + 柔和灰绿品牌色 `#6a9a92` + 暖玫瑰强调 `#b87070`

**字体**：DM Serif Display（标题） · Outfit（正文） · JetBrains Mono（代码） · Noto Serif SC（中文衬线）

**组件覆写**：Element Plus 全套深色主题（按钮/输入/表格/标签/对话框/下拉菜单/分页/加载/空状态）

**动画策略**：仅使用 `opacity` + `transform`（GPU 合成层），不使用 `backdrop-filter` 或 `filter: blur()`

## 8. 认证与权限

- **JWT 无状态认证**：登录返回 accessToken，前端存 localStorage，请求自动携带
- **四角色路由守卫**：USER / DOCTOR / OPERATOR / ADMIN，前端路由 + 后端接口双重校验
- **默认账号**：user/doctor/operator/admin，密码均为 123456

## 9. 部署方案

**推荐**：本地 Java + Docker MySQL/Redis

```bash
# MySQL
docker run -d --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root mysql:8

# 后端
cd backend && mvn spring-boot:run

# 前端（开发）
cd frontend && npm run dev

# 前端（生产）
cd frontend && npm run build  # 产出在 frontend/dist/
```

## 10. 待实现功能

- PDF 报告导出
- 多年聚合对比分析
- 微信/支付宝支付对接
- 短信通知
- 健康档案管理
- 移动端适配
