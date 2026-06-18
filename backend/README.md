# 熙心健康体检平台后端

## 运行环境
- `Java 8`
- `Maven 3.9+`
- `MySQL 8`
- 数据库名：`xixin_health`

## 初始化步骤
1. 执行建表脚本：`D:\projects\熙心健康体检平台\sql\xixin_health.sql`
2. 执行演示数据脚本：`D:\projects\熙心健康体检平台\backend\sql\xixin_health_demo.sql`
3. 检查数据库配置：`D:\projects\熙心健康体检平台\backend\src\main\resources\application.yml`
4. 启动后端：

```powershell
D:\tools\apache-maven-3.9.11\bin\mvn.cmd spring-boot:run
```

## 常用命令
```powershell
D:\tools\apache-maven-3.9.11\bin\mvn.cmd -DskipTests compile
```

## 演示账号
- 用户：`user / 123456`
- 医生：`doctor / 123456`
- 运营：`operator / 123456`
- 管理员：`admin / 123456`

## 当前已实现模块
- 认证与数据库登录：`/api/auth/**`
- 用户预约：`/api/appointments/**`
- 用户订单与模拟支付：`/api/orders/**`
- 用户退款申请：`/api/orders/{orderNo}/refund`
- 运营退款审核：`/api/operator/refunds/**`
- 运营套餐管理：`/api/operator/packages/**`
- 运营排班管理：`/api/operator/schedules/**`
- 运营预约管理：`/api/operator/appointments/**`
- 运营订单管理：`/api/operator/orders/**`
- 运营数据看板：`/api/operator/analytics/**`
- 体检任务与导检：`/api/exam-tasks/**`、`/api/admin/exam-tasks/**`
- 医生结果录入：`/api/doctor/exam-tasks/**`
- 报告生成、审核、发布：`/api/reports/**`、`/api/admin/reports/**`
- 咨询医生：`/api/doctor-consultations/**`、`/api/doctor/consultations/**`
- 双报告对比与健康建议：`/api/report-compare/tasks/**`、`/api/health-advices/**`
- 医生异常分析与工作量统计：`/api/doctor/analytics/**`、`/api/admin/doctor-analytics/**`

## 当前实现边界
- 当前系统仍以登录用户链路为主，游客公开浏览接口尚未实现
- 游客后续建议统一走 `/api/public/**`
- 登录已切换为数据库认证
- 后台账号采用 `staff_account + staff_role_rel`
- 用户登录采用 `user.user_no + password_hash`
- 退款仍为整单退款，审核通过后同步 mock 成功
- 运营导出接口当前返回结构化 JSON，不生成文件
- 运营分析当前以实时聚合为主，不依赖完整离线统计任务
- 医生分析当前也采用实时聚合，不新增统计表
- 报告导出 PDF 仍未实现

## 推荐联调顺序
1. 先跑用户主链路：预约 -> 订单 -> 支付 -> 体检 -> 报告
2. 再跑对比链路：第二份报告 -> 报告对比 -> 健康建议
3. 再跑咨询医生链路
4. 再跑医生分析链路：异常总览 -> 项目分布 -> 高风险用户 -> 异常趋势 -> 工作量总览 -> 工作量趋势
5. 最后跑运营端：登录 -> 套餐/排班 -> 预约/订单 -> 退款 -> 看板
