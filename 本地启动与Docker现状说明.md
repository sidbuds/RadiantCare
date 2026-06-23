# 本地启动与 Docker 说明

## 启动方式概览

本项目支持两种启动方式：

| 方式 | 适用场景 | 启动速度 |
|---|---|---|
| Docker Compose 一键启动 | 环境复现、联调、新人上手 | 首次较慢，后续秒启 |
| 本地分别启动 | 日常开发调试 | 快 |

两种方式的端口统一为：前端 `5173`、后端 `18080`。

---

## 方式一：Docker Compose 一键启动

### 前置条件

- Docker Desktop 已安装并运行
- Java 8+ 和 Maven 3.6+（用于本地构建后端 JAR）

### 文件说明

| 文件 | 用途 |
|---|---|
| `docker-compose.yml` | 编排 mysql / redis / backend / frontend 四个服务 |
| `backend/Dockerfile` | 后端镜像（复制本地构建的 JAR） |
| `frontend/Dockerfile` | 前端多阶段构建（node:22 → nginx:alpine） |
| `frontend/nginx.conf` | Nginx 配置：SPA 路由 + API 反向代理 |
| `docker-up.ps1` | 一键构建启动脚本 |
| `.env` | 环境变量默认值（不入 Git） |

### 快速启动

```powershell
# 一键构建后端 JAR 并启动全部服务
.\docker-up.ps1
```

启动后访问：

| 服务 | 地址 |
|---|---|
| 前端 | http://127.0.0.1:5173 |
| 后端 API | http://127.0.0.1:18080 |
| Swagger UI | http://127.0.0.1:18080/swagger-ui.html |
| MySQL | 127.0.0.1:3306 |
| Redis | 127.0.0.1:6379 |

### 常用命令

```powershell
# 重建镜像并启动
.\docker-up.ps1 -Build

# 停止所有服务
.\docker-up.ps1 -Down

# 查看实时日志
.\docker-up.ps1 -Logs

# 或直接使用 docker compose
docker compose up -d           # 启动
docker compose up -d --build   # 重建并启动
docker compose down            # 停止
docker compose down -v         # 停止并删除数据卷（清空数据库）
docker compose logs -f         # 查看日志
docker compose ps              # 查看状态
```

### 后端代码变更后

后端采用「本地构建 JAR → 容器运行」模式，代码修改后需重新打包：

```powershell
cd backend
D:\tools\apache-maven-3.9.11\bin\mvn.cmd package -DskipTests
docker compose up -d --build backend
```

### 环境变量

在项目根目录 `.env` 文件中配置（已有默认值，一般无需修改）：

```env
DB_PASSWORD=gwtzn7k8
JWT_SECRET=xixin-health-demo-secret
SPRING_PROFILES_ACTIVE=dev
```

### 数据库初始化

Docker Compose 启动时会自动执行 `sql/` 目录下的初始化脚本：

1. `xixin_health_clean.sql` — 建库建表
2. `xixin_demo_data.sql` — 演示数据

首次启动自动导入，数据持久化在 Docker Volume `xixin-health_mysql-data` 中。

如需重置数据库：

```powershell
docker compose down -v
docker compose up -d
```

---

## 方式二：本地分别启动

适合日常开发，修改代码后热更新更快。

### 前置条件

- Java 8+
- Maven 3.6+
- Node.js 18+
- Docker Desktop（仅用于运行 MySQL / Redis）

### 1. 启动 MySQL 和 Redis

```powershell
docker start rongxiaotong-mysql rongxiaotong-redis
```

或使用本项目的容器：

```powershell
docker compose up -d mysql redis
```

### 2. 启动后端

```powershell
cd D:\projects\熙心健康体检平台\backend
D:\tools\apache-maven-3.9.11\bin\mvn.cmd spring-boot:run
```

访问 http://127.0.0.1:18080 ，根路径返回 `403` 表示已启动（被 Spring Security 拦截）。

### 3. 启动前端

```powershell
cd D:\projects\熙心健康体检平台\frontend
npm run dev -- --host 127.0.0.1 --port 5173
```

访问 http://127.0.0.1:5173 。

---

## 端口规划

| 端口 | 服务 | 说明 |
|---|---|---|
| 5173 | 前端 | Vite 开发服务器 / Nginx |
| 18080 | 后端 | Spring Boot |
| 3306 | MySQL | 数据库 |
| 6379 | Redis | 缓存 |
| 8080 | — | 已被其他项目占用，勿用 |

## 演示账号

| 角色 | 用户名 | 密码 |
|---|---|---|
| 用户 | user | 123456 |
| 医生 | doctor | 123456 |
| 运营 | operator | 123456 |
| 管理员 | admin | 123456 |

## 已知问题

- 后端采用 `openjdk:8-jre` 镜像运行，Docker Hub 拉取受限时需提前拉取或配置镜像源。
- 前端构建需要 Node.js 22+，Dockerfile 中使用 `node:22-alpine`。
- `8080` 端口已被其他项目容器占用，本项目不使用该端口。
