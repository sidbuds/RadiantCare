# 熙心健康体检平台 Docker 设计文档

## 1. 设计目标
- 使用 Docker 运行 MySQL、Redis 和后端服务。
- 支持本地 Java 8 运行应用，也支持后端整体容器化。
- 当前推荐方式：本地运行 Java，Docker 运行 MySQL / Redis。

## 2. 容器规划
| 服务 | 说明 |
|---|---|
| `rongxiaotong-mysql` | MySQL 8 数据库 |
| `rongxiaotong-redis` | Redis 7 缓存 |
| `xixin-gateway` | 网关服务 |
| `xixin-user` | 用户服务 |
| `xixin-appointment` | 预约服务 |
| `xixin-order` | 订单服务 |
| `xixin-exam` | 到院导检与体检执行服务 |
| `xixin-report` | 报告生成与审核服务 |
| `xixin-analytics` | 历年对比与健康建议服务 |
| `xixin-doctor-consultation` | 咨询医生服务 |

## 3. 基础镜像建议
- Java 服务镜像：`eclipse-temurin:8-jre`
- MySQL 镜像：`mysql:8.0`
- Redis 镜像：`redis:7`

## 4. 当前推荐运行方式

### 4.1 方式一：本地运行 Java，Docker 运行中间件
- 本地使用 `D:\jdk\bin\java.exe`
- Docker 运行 `MySQL 8` 和 `Redis 7`
- 适合开发调试，启动更快

### 4.2 方式二：后端和中间件全部 Docker 化
- 适合集成测试、联调、部署预演

## 5. 与本项目链路相关的容器职责
- `xixin-exam`：生成体检任务、导检路线、处理医生录入和附件
- `xixin-report`：根据套餐模板归集结果、生成报告、处理审核发布
- `xixin-analytics`：做历年对比、风险评分、健康建议生成
- `xixin-doctor-consultation`：处理用户咨询医生、医生回复、咨询状态流转

## 6. 数据导入说明
- 主 SQL 脚本：`D:\projects\熙心健康体检平台\sql\xixin_health.sql`
- 当前首期 SQL 只包含建库、建表、索引和约束，不包含初始化数据。
- 推荐先启动 MySQL 容器，再导入 SQL。

## 7. 开发建议
- 本地开发优先用 Docker 跑数据库和缓存。
- 若后续要做多人协作或环境复现，再补 `docker-compose.yml`。
