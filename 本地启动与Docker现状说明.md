# 本地启动与 Docker 现状说明

## 当前确认结果

- 当前仓库**不是完整 Docker 化启动**项目。
- 当前更接近的运行方式是：
  - `MySQL` / `Redis` 使用 Docker 运行
  - 后端使用本地 Java + Maven 启动
  - 前端使用本地 Node + Vite 启动

## 已确认的 Docker 容器

当前机器上和本项目直接相关的容器有：

- `rongxiaotong-mysql`
  - 镜像：`mysql:8.0`
  - 端口：`3306`
- `rongxiaotong-redis`
  - 镜像：`redis:6.2`
  - 端口：`6379`

另外，`8080` 端口上运行的是其他项目容器：

- `interview-app`
  - 端口：`8080 -> 8080`

这说明本项目原本依赖的数据库和缓存很可能长期由 Docker 提供，但应用本身不是通过当前仓库里的 Docker 文件启动的。

## 当前仓库状态

仓库中目前没有发现以下文件：

- `Dockerfile`
- `docker-compose.yml`
- `docker-compose.yaml`
- `compose.yml`
- `compose.yaml`

因此，无法从当前目录直接用 Compose 一键启动整套服务。

## 本地启动建议

为了避免和其他项目冲突，当前项目本地开发端口已调整为：

- 前端：`5173`
- 后端：`18080`

对应配置文件：

- [frontend/vite.config.ts](D:\projects\熙心健康体检平台\frontend\vite.config.ts)
- [backend/src/main/resources/application.yml](D:\projects\熙心健康体检平台\backend\src\main\resources\application.yml)

## 启动方式

### 1. 后端

后端使用本地 Java 启动，访问地址：

- `http://127.0.0.1:18080`

如果访问根路径返回 `403`，通常表示 Spring Boot 已经启动，只是根路径被 Spring Security 拦截。

启动命令：

```powershell
cd D:\projects\熙心健康体检平台\backend
D:\tools\apache-maven-3.9.11\bin\mvn.cmd spring-boot:run
```

### 2. 前端

前端使用本地 Vite 启动，访问地址：

- `http://127.0.0.1:5173`

启动命令：

```powershell
cd D:\projects\熙心健康体检平台\frontend
npm run dev -- --host 127.0.0.1 --port 5173
```

## 已知问题

- 当前 Codex 终端环境里，Vite 后台保活不稳定，可能出现日志显示 `ready`，但进程很快退出的情况。
- 如果浏览器报 `ERR_CONNECTION_REFUSED`，优先检查前端开发服务器是否仍在运行。
- `8080` 端口不要直接拿来启动本项目后端，因为当前机器上已有其他容器占用。

## 结论

目前最符合实际的开发方式是：

1. 保留 Docker 中的 `MySQL` / `Redis`
2. 本地启动当前仓库的后端
3. 本地启动当前仓库的前端
4. 使用独立端口 `18080 + 5173` 避免影响其他项目
