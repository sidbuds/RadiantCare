# 熙心健康体检平台 — RadiantCare

全链路体检预约与健康管理平台。覆盖从套餐浏览、在线预约、到检导引、结果录入、报告生成审核、在线咨询到历年对比的完整闭环。

## 技术栈

| 层 | 技术 |
|---|---|
| 前端 | Vue 3.5 + TypeScript + Vite 8 + Element Plus 2.14 + ECharts 6 + Pinia 3 |
| 后端 | Java 8 + Spring Boot 2.7 + Spring Security + JWT + MyBatis-Plus 3.5 |
| 数据库 | MySQL 8 |
| API 文档 | SpringDoc OpenAPI (Swagger UI) |

## 快速启动

### 方式一：Docker Compose 一键启动（推荐）

前置条件：Docker Desktop、Java 8+、Maven 3.6+

```powershell
# 一键构建并启动全部服务（MySQL / Redis / 后端 / 前端）
.\docker-up.ps1
```

| 服务 | 地址 |
|---|---|
| 前端 | http://127.0.0.1:5173 |
| 后端 API | http://127.0.0.1:18080 |
| Swagger UI | http://127.0.0.1:18080/swagger-ui.html |

```powershell
.\docker-up.ps1 -Down    # 停止
.\docker-up.ps1 -Build   # 重建镜像
.\docker-up.ps1 -Logs    # 查看日志
```

### 方式二：本地分别启动

前置条件：Node.js 18+、Java 8+、Maven 3.6+、Docker（运行 MySQL/Redis）

```bash
# 数据库（使用 Docker）
docker compose up -d mysql redis

# 后端
cd backend
mvn spring-boot:run

# 前端
cd frontend
npm install
npm run dev
```

### 演示账号
| 角色 | 用户名 | 密码 |
|---|---|---|
| 用户 | user | 123456 |
| 医生 | doctor | 123456 |
| 运营 | operator | 123456 |
| 管理员 | admin | 123456 |

## 项目结构

```
├── frontend/               # Vue 3 SPA
│   ├── src/
│   │   ├── views/          # 31 个页面（guest/user/doctor/operator/admin）
│   │   ├── layouts/        # DefaultLayout + AdminLayout
│   │   ├── stores/         # Pinia 状态管理
│   │   ├── api/            # Axios 请求模块（12 个模块，64 个接口）
│   │   ├── types/          # TypeScript 类型定义
│   │   ├── router/         # 路由 + 权限守卫
│   │   └── styles/         # 全局设计系统（暗/亮主题）
│   ├── Dockerfile          # 前端容器化构建
│   └── nginx.conf          # Nginx 配置（SPA + API 代理）
├── backend/                # Spring Boot 后端
│   ├── src/main/java/com/xixin/health/
│   │   ├── auth/           # 登录认证
│   │   ├── security/       # JWT + Spring Security
│   │   ├── appointment/    # 预约模块
│   │   ├── order/          # 订单模块
│   │   ├── exam/           # 检查执行模块
│   │   ├── report/         # 报告模块
│   │   ├── compare/        # 历年对比模块
│   │   ├── consultation/   # 咨询模块
│   │   ├── operator/       # 运营管理
│   │   ├── doctor/         # 医生分析
│   │   ├── publicapi/      # 游客公开接口
│   │   └── common/         # 公共组件（异常处理、CORS、Swagger）
│   └── Dockerfile          # 后端容器化构建
├── sql/                    # 数据库脚本（43 张表）
├── docker-compose.yml      # Docker Compose 编排
├── docker-up.ps1           # 一键启动脚本
└── .env.example            # 环境变量模板
```

## 功能模块

### 访客端
套餐浏览 · 体检中心查询 · 检查流程指引 · 常见问题

### 用户端
在线预约 · 订单支付 · 导检路线 · 报告查看 · 历年对比 · 健康建议 · 在线咨询

### 医生端
待检任务 · 结果录入 · 咨询回复 · 异常分析 · 工作量统计

### 运营端
套餐管理 · 排班管理 · 预约/订单/退款管理 · 运营看板

### 管理端
任务生成 · 报告发布 · 咨询分配 · 医生分析

## 设计系统

深色/亮色双主题设计系统，定义在 `frontend/src/styles/index.scss`：
- 配色：深炭灰底 + 柔和灰绿品牌色 + 暖玫瑰强调色
- 字体：DM Serif Display（标题）+ Outfit（正文）+ JetBrains Mono（代码）
- 组件：统一的 Element Plus 深色覆写
- 支持 `data-theme="light"` 切换亮色主题

## 环境变量

参考 `.env.example` 配置：

```bash
DB_URL=jdbc:mysql://127.0.0.1:3306/xixin_health?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
DB_USERNAME=root
DB_PASSWORD=your_password
JWT_SECRET=your_jwt_secret
SERVER_PORT=18080
SPRING_PROFILES_ACTIVE=dev
```

## 文档

| 文档 | 内容 |
|---|---|
| [需求文档](需求文档.md) | 项目背景、目标、角色定义 |
| [全链路业务说明](全链路业务说明文档.md) | 各角色完整业务链路 |
| [数据库设计](数据库设计文档.md) | 表结构设计与规范 |
| [后端技术栈与API设计](后端技术栈与API设计.md) | 后端架构与接口设计 |
| [前端技术栈与界面API设计](前端技术栈与界面API设计.md) | 前端架构与页面设计 |
| [Docker设计](Docker设计文档.md) | 容器化部署方案 |
| [本地启动与Docker说明](本地启动与Docker现状说明.md) | 启动方式详解（含 Docker Compose） |
| [链路映射文档](链路-表-API-代码映射文档.md) | 业务链路-表-API-代码对应关系 |
| [接口联调手册](backend/接口联调手册.md) | 全链路 curl 联调示例 |
