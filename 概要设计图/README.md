# 熙心健康体检平台 - 模块类图说明

## 文件说明

| 文件名 | 说明 |
|--------|------|
| `模块类图.md` | 分模块详细类图（PlantUML格式）+ 关系说明 |
| `模块类图_统一视图.md` | 统一视图类图（Mermaid格式）+ 生成提示词 |
| `模块类图.puml` | 可直接渲染的PlantUML文件 |

---

## 模块划分

```
                    熙心健康体检平台
                         |
    +----+----+----+----+----+----+----+----+
    |    |    |    |    |    |    |    |    |
  用户  订单  预约  咨询  报告  体检  运营  体检  质控
  管理  管理  管理  管理  管理  套餐  管理  数据  管理
                        管理        分析
```

---

## 模块职责

| 模块 | 核心职责 | 主要实体 |
|------|----------|----------|
| 用户管理 | 用户注册登录、后台账号管理、角色权限 | User, StaffAccount, StaffRoleRel |
| 预约管理 | 预约创建/取消、套餐选择、资源时段管理 | Appointment, ExamPackage, ResourceCapacity |
| 订单管理 | 订单创建/支付/退款、订单明细 | Order, OrderItem, PaymentRecord, RefundApply |
| 体检执行 | 体检任务生成、任务项执行、结果录入 | ExamTask, ExamTaskItem, ExamResult |
| 报告管理 | 报告生成/审核/发布、模板管理 | ExamReport, DoctorReviewRecord, ReportTemplate |
| 咨询管理 | 咨询创建/分配/回复、咨询记录 | DoctorConsultation, DoctorConsultationReply |
| 数据分析 | 报告对比、指标趋势、风险评分、健康建议 | ReportCompareTask, HealthRiskScore, HealthAdviceRecord |
| 运营管理 | 排班管理、资源容量、运营统计 | ResourceCapacity, StatDailyReport |
| 质控管理 | 体检质量控制、异常监控 | ExamResult, DoctorReviewRecord |

---

## 关系类型说明

| 关系类型 | UML符号 | PlantUML语法 | 说明 |
|----------|---------|--------------|------|
| 组合 | ◆ | `*--` | 强关联，部分不能独立于整体存在 |
| 聚合 | ◇ | `o--` | 弱关联，部分可以独立于整体存在 |
| 关联 | → | `--` | 结构性关系，持有引用 |
| 依赖 | ╌> | `..>` | 使用关系，临时引用 |

---

## 核心业务流程

### 1. 预约流程
```
User → Appointment → ExamPackage → ResourceCapacity
```

### 2. 订单流程
```
Appointment → Order → OrderItem → PaymentRecord
```

### 3. 体检流程
```
Appointment → ExamTask → ExamTaskItem → ExamResult
```

### 4. 报告流程
```
ExamResult → ExamReportItem → ExamReport → DoctorReviewRecord
```

### 5. 咨询流程
```
ExamReport → DoctorConsultation → DoctorConsultationReply
```

### 6. 分析流程
```
ExamReport → ReportCompareTask → HealthRiskScore → HealthAdviceRecord
```

---

## 生成提示词

### 提示词1 - UML类图生成

```
请根据以下熙心健康体检平台的模块划分和实体关系，生成一张清晰的UML类图：

【模块划分】
1. 用户管理：User, StaffAccount, StaffRoleRel
2. 预约管理：Appointment, ExamPackage, ExamPackageItem, ResourceCapacity
3. 订单管理：Order, OrderItem, PaymentRecord, RefundApply
4. 体检执行：ExamTask, ExamTaskItem, ExamResult, ExamDepartmentRoute
5. 报告管理：ExamReport, ExamReportItem, DoctorReviewRecord, ReportTemplate
6. 咨询管理：DoctorConsultation, DoctorConsultationReply
7. 数据分析：ReportCompareTask, ReportCompareResult, HealthRiskScore, HealthAdviceRecord, IndicatorTrendTag

【实体关系】
- User 1..* → StaffAccount（关联）
- StaffAccount 1..* → StaffRoleRel（组合）
- User 1..* → Appointment（创建）
- Appointment *..1 → ExamPackage（选择）
- ExamPackage 1..* → ExamPackageItem（组合）
- Appointment *..1 → ResourceCapacity（占用）
- Appointment *..1 → Order（生成）
- Order 1..* → OrderItem（组合）
- Order 1..* → PaymentRecord（聚合）
- Order 1..* → RefundApply（关联）
- Appointment *..1 → ExamTask（生成）
- ExamTask 1..* → ExamTaskItem（组合）
- ExamTaskItem 1..* → ExamResult（聚合）
- ExamTaskItem *..1 → ExamDepartmentRoute（关联）
- ExamResult *..1 → ExamReportItem（关联）
- ExamReportItem *..1 → ExamReport（组合）
- ExamReport 1..* → DoctorReviewRecord（聚合）
- ExamReport *..1 → ReportTemplate（使用）
- DoctorConsultation 1..* → DoctorConsultationReply（组合）
- DoctorConsultation *..1 → ExamReport（关联）
- ReportCompareTask 1..* → ReportCompareResult（组合）
- ReportCompareTask *..1 → ExamReport（基线/对比）
- ReportCompareTask 1..1 → HealthRiskScore（关联）
- ReportCompareTask 1..* → HealthAdviceRecord（聚合）
- StaffAccount 1..* → ExamResult（录入）
- StaffAccount 1..* → DoctorReviewRecord（审核）
- StaffAccount 1..* → DoctorConsultation（回复）

【排版要求】
- 按模块分区，用户管理在左上，预约管理在右上
- 订单管理在中上，体检执行在中间
- 报告管理在右中，咨询管理在右下
- 数据分析在左下
- 连线不要交叉，箭头清晰可见
- 每个类显示属性和方法
- 类名、属性、方法用中文注释
```

### 提示词2 - Mermaid格式转换

````
请将以下UML类图转换为Mermaid格式，要求：
1. 使用classDiagram语法
2. 按模块分组，使用namespace
3. 显示类的属性和方法
4. 明确标注关系类型（组合、聚合、关联）
5. 关系箭头清晰，不重叠

模块和实体：
- 用户管理：User, StaffAccount, StaffRoleRel
- 预约管理：Appointment, ExamPackage, ExamPackageItem, ResourceCapacity
- 订单管理：Order, OrderItem, PaymentRecord, RefundApply
- 体检执行：ExamTask, ExamTaskItem, ExamResult, ExamDepartmentRoute
- 报告管理：ExamReport, ExamReportItem, DoctorReviewRecord, ReportTemplate
- 咨询管理：DoctorConsultation, DoctorConsultationReply
- 数据分析：ReportCompareTask, ReportCompareResult, HealthRiskScore, HealthAdviceRecord
````

### 提示词3 - PlantUML生成

```
请生成PlantUML格式的类图代码，要求：
1. 使用package语法按模块分组
2. 每个类显示属性和方法
3. 使用skinparam设置样式美化
4. 关系线使用ortho模式避免交叉
5. 每个模块使用不同背景色区分

模块：用户管理、预约管理、订单管理、体检执行、报告管理、咨询管理、数据分析

关系：
- 组合关系：StaffAccount-StaffRoleRel, ExamPackage-ExamPackageItem, Order-OrderItem, ExamTask-ExamTaskItem, ExamReport-ExamReportItem, DoctorConsultation-DoctorConsultationReply, ReportCompareTask-ReportCompareResult
- 聚合关系：Order-PaymentRecord, ExamTaskItem-ExamResult, ExamReport-DoctorReviewRecord, ReportCompareTask-HealthAdviceRecord
- 关联关系：User-StaffAccount, User-Appointment, Appointment-ExamPackage, Appointment-ResourceCapacity, Appointment-Order, Appointment-ExamTask, ExamTaskItem-ExamDepartmentRoute, ExamResult-ExamReportItem, ExamReport-ReportTemplate, DoctorConsultation-ExamReport, ReportCompareTask-ExamReport, ReportCompareTask-HealthRiskScore, StaffAccount-ExamResult, StaffAccount-DoctorReviewRecord, StaffAccount-DoctorConsultation
```

---

## 渲染方式

### PlantUML渲染
1. 使用VS Code插件：PlantUML
2. 使用在线工具：https://www.plantuml.com/plantuml/
3. 使用IntelliJ插件：PlantUML Integration

### Mermaid渲染
1. 使用VS Code插件：Markdown Preview Mermaid Support
2. 使用在线工具：https://mermaid.live/
3. GitHub/GitLab原生支持

---

## 文件使用说明

1. **快速查看**：使用 `模块类图_统一视图.md` 中的Mermaid代码
2. **正式文档**：使用 `模块类图.puml` 渲染为图片
3. **详细说明**：查看 `模块类图.md` 中的分模块详细关系
