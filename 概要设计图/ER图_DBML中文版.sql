// ============================================
// 熙心健康体检平台 - DBML格式（中文版）
// 复制到 https://dbdiagram.io/d 即可生成ER图
// ============================================

// ========== 用户体系 ==========
Table 用户 {
  用户ID BIGINT [pk, increment]
  用户编号 VARCHAR(32) [not null, unique]
  姓名 VARCHAR(64) [not null]
  性别 TINYINT
  手机号 VARCHAR(20) [not null, unique]
  密码哈希 VARCHAR(128)
  状态 TINYINT [default: 1]
}

Table 后台账号 {
  账号ID BIGINT [pk, increment]
  用户名 VARCHAR(64) [not null, unique]
  密码哈希 VARCHAR(128) [not null]
  显示名称 VARCHAR(64) [not null]
  绑定用户ID BIGINT [not null]
  状态 TINYINT [default: 1]
}

Table 角色关联 {
  关联ID BIGINT [pk, increment]
  账号ID BIGINT [not null]
  角色编码 VARCHAR(32) [not null]
}

// ========== 预约套餐 ==========
Table 体检套餐 {
  套餐ID BIGINT [pk, increment]
  套餐编码 VARCHAR(32) [not null, unique]
  套餐名称 VARCHAR(128) [not null]
  分类 VARCHAR(32)
  价格 DECIMAL(12,2) [not null, default: 0]
  状态 TINYINT [default: 1]
}

Table 套餐项目 {
  项目ID BIGINT [pk, increment]
  套餐ID BIGINT [not null]
  项目编码 VARCHAR(32) [not null]
  项目名称 VARCHAR(128) [not null]
  单位 VARCHAR(32)
  参考范围 VARCHAR(128)
  排序号 INT [default: 0]
}

Table 资源容量 {
  容量ID BIGINT [pk, increment]
  中心编码 VARCHAR(32) [not null]
  预约日期 DATE [not null]
  时段编码 VARCHAR(32) [not null]
  总容量 INT [default: 0]
  已使用 INT [default: 0]
  状态 TINYINT [default: 1]
}

Table 预约 {
  预约ID BIGINT [pk, increment]
  预约单号 VARCHAR(32) [not null, unique]
  用户ID BIGINT [not null]
  套餐ID BIGINT [not null]
  中心编码 VARCHAR(32) [not null]
  预约日期 DATE [not null]
  时段编码 VARCHAR(32) [not null]
  状态 TINYINT [default: 0]
}

// ========== 订单支付 ==========
Table 订单 {
  订单ID BIGINT [pk, increment]
  订单号 VARCHAR(32) [not null, unique]
  预约ID BIGINT [not null]
  用户ID BIGINT [not null]
  套餐ID BIGINT [not null]
  总金额 DECIMAL(12,2) [default: 0]
  实付金额 DECIMAL(12,2) [default: 0]
  优惠金额 DECIMAL(12,2) [default: 0]
  状态 TINYINT [default: 0]
  支付渠道 VARCHAR(32)
}

Table 订单明细 {
  明细ID BIGINT [pk, increment]
  订单ID BIGINT [not null]
  项目类型 TINYINT [default: 0]
  项目编码 VARCHAR(32) [not null]
  项目名称 VARCHAR(128) [not null]
  数量 INT [default: 1]
  单价 DECIMAL(12,2) [default: 0]
  金额 DECIMAL(12,2) [default: 0]
}

Table 支付记录 {
  支付ID BIGINT [pk, increment]
  支付流水号 VARCHAR(32) [not null, unique]
  订单ID BIGINT [not null]
  支付渠道 VARCHAR(32) [not null]
  第三方交易号 VARCHAR(64)
  支付金额 DECIMAL(12,2) [default: 0]
  状态 TINYINT [default: 0]
}

// ========== 体检执行 ==========
Table 体检任务 {
  任务ID BIGINT [pk, increment]
  任务号 VARCHAR(32) [not null, unique]
  预约ID BIGINT [not null]
  订单ID BIGINT [not null]
  用户ID BIGINT [not null]
  体检日期 DATE [not null]
  任务状态 TINYINT [default: 0]
  报告状态 TINYINT [default: 0]
}

Table 导检路线 {
  路线ID BIGINT [pk, increment]
  路线编码 VARCHAR(32) [not null, unique]
  中心编码 VARCHAR(32) [not null]
  套餐ID BIGINT [not null]
  项目编码 VARCHAR(32) [not null]
  科室编码 VARCHAR(32) [not null]
  科室名称 VARCHAR(128) [not null]
  房间号 VARCHAR(64)
  检查顺序 INT [default: 0]
}

Table 任务项 {
  任务项ID BIGINT [pk, increment]
  任务项号 VARCHAR(32) [not null, unique]
  任务ID BIGINT [not null]
  项目编码 VARCHAR(32) [not null]
  项目名称 VARCHAR(128) [not null]
  科室编码 VARCHAR(32) [not null]
  医生ID BIGINT
  项目状态 TINYINT [default: 0]
}

Table 体检结果 {
  结果ID BIGINT [pk, increment]
  结果号 VARCHAR(32) [not null, unique]
  任务ID BIGINT [not null]
  任务项ID BIGINT [not null]
  用户ID BIGINT [not null]
  项目编码 VARCHAR(32) [not null]
  结果类型 VARCHAR(32) [not null]
  结果值 VARCHAR(256)
  结果数值 DECIMAL(18,4)
  是否异常 TINYINT [default: 0]
  异常级别 TINYINT [default: 0]
  录入医生ID BIGINT
}

// ========== 报告管理 ==========
Table 报告模板 {
  模板ID BIGINT [pk, increment]
  模板编码 VARCHAR(32) [not null, unique]
  模板名称 VARCHAR(128) [not null]
  套餐ID BIGINT [not null]
  模板类型 VARCHAR(32) [not null]
  状态 TINYINT [default: 1]
}

Table 体检报告 {
  报告ID BIGINT [pk, increment]
  报告编号 VARCHAR(32) [not null, unique]
  预约ID BIGINT [not null]
  任务ID BIGINT [not null]
  用户ID BIGINT [not null]
  套餐ID BIGINT [not null]
  模板ID BIGINT
  报告日期 DATE [not null]
  总体结论 TEXT
  状态 TINYINT [default: 0]
  PDF地址 VARCHAR(512)
}

Table 报告指标 {
  指标ID BIGINT [pk, increment]
  报告ID BIGINT [not null]
  项目编码 VARCHAR(32) [not null]
  项目名称 VARCHAR(128) [not null]
  结果值 VARCHAR(256)
  结果数值 DECIMAL(18,4)
  是否异常 TINYINT [default: 0]
  异常级别 TINYINT [default: 0]
  排序号 INT [default: 0]
}

Table 审核记录 {
  审核ID BIGINT [pk, increment]
  审核流水号 VARCHAR(32) [not null, unique]
  报告ID BIGINT [not null]
  任务ID BIGINT [not null]
  审核阶段 VARCHAR(32) [not null]
  审核结果 VARCHAR(32) [not null]
  审核意见 VARCHAR(512)
  审核人ID BIGINT
}

// ========== 咨询管理 ==========
Table 咨询工单 {
  咨询ID BIGINT [pk, increment]
  咨询单号 VARCHAR(32) [not null, unique]
  用户ID BIGINT [not null]
  医生ID BIGINT
  报告ID BIGINT
  来源页面 VARCHAR(32) [not null]
  咨询类型 VARCHAR(32) [not null]
  咨询标题 VARCHAR(128)
  咨询内容 TEXT [not null]
  状态 TINYINT [default: 0]
  优先级 TINYINT [default: 0]
}

Table 咨询回复 {
  回复ID BIGINT [pk, increment]
  咨询ID BIGINT [not null]
  咨询单号 VARCHAR(32) [not null]
  回复角色 VARCHAR(16) [not null]
  回复人ID BIGINT
  回复人姓名 VARCHAR(64)
  回复内容 TEXT [not null]
  附件地址 VARCHAR(512)
  回复时间 DATETIME [not null]
}

// ========== 数据分析 ==========
Table 对比任务 {
  任务ID BIGINT [pk, increment]
  任务号 VARCHAR(32) [not null, unique]
  用户ID BIGINT [not null]
  基线报告ID BIGINT [not null]
  对比报告ID BIGINT [not null]
  状态 TINYINT [default: 0]
}

Table 对比结果 {
  结果ID BIGINT [pk, increment]
  任务ID BIGINT [not null]
  项目编码 VARCHAR(32) [not null]
  项目名称 VARCHAR(128) [not null]
  基线值 VARCHAR(256)
  对比值 VARCHAR(256)
  变化值 DECIMAL(18,4)
  变化率 DECIMAL(10,4)
  趋势 TINYINT [default: 0]
}

Table 风险评分 {
  评分ID BIGINT [pk, increment]
  任务ID BIGINT [not null, unique]
  用户ID BIGINT [not null]
  总分 DECIMAL(10,2) [default: 0]
  异常得分 DECIMAL(10,2) [default: 0]
  趋势得分 DECIMAL(10,2) [default: 0]
  风险等级 TINYINT [default: 0]
}

Table 健康建议 {
  建议ID BIGINT [pk, increment]
  建议单号 VARCHAR(32) [not null, unique]
  用户ID BIGINT [not null]
  报告ID BIGINT
  对比任务ID BIGINT
  建议类型 VARCHAR(32) [not null]
  风险等级 TINYINT [default: 0]
  建议标题 VARCHAR(128) [not null]
  建议内容 TEXT [not null]
}

// ============================================
// 关系定义
// ============================================

// 用户体系
Ref: 后台账号.绑定用户 > 用户.用户ID
Ref: 角色关联.账号 > 后台账号.账号ID

// 预约套餐
Ref: 套餐项目.所属套餐 > 体检套餐.套餐ID
Ref: 预约.用户 > 用户.用户ID
Ref: 预约.选择套餐 > 体检套餐.套餐ID

// 订单支付
Ref: 订单.关联预约 > 预约.预约ID
Ref: 订单.用户 > 用户.用户ID
Ref: 订单明细.所属订单 > 订单.订单ID
Ref: 支付记录.关联订单 > 订单.订单ID

// 体检执行
Ref: 体检任务.关联预约 > 预约.预约ID
Ref: 体检任务.关联订单 > 订单.订单ID
Ref: 体检任务.用户 > 用户.用户ID
Ref: 任务项.所属任务 > 体检任务.任务ID
Ref: 任务项.负责医生 > 后台账号.账号ID
Ref: 体检结果.关联任务 > 体检任务.任务ID
Ref: 体检结果.关联任务项 > 任务项.任务项ID
Ref: 体检结果.用户 > 用户.用户ID
Ref: 体检结果.录入医生 > 后台账号.账号ID
Ref: 导检路线.所属套餐 > 体检套餐.套餐ID

// 报告管理
Ref: 报告模板.所属套餐 > 体检套餐.套餐ID
Ref: 体检报告.关联预约 > 预约.预约ID
Ref: 体检报告.关联任务 > 体检任务.任务ID
Ref: 体检报告.用户 > 用户.用户ID
Ref: 体检报告.使用模板 > 报告模板.模板ID
Ref: 报告指标.所属报告 > 体检报告.报告ID
Ref: 审核记录.关联报告 > 体检报告.报告ID
Ref: 审核记录.关联任务 > 体检任务.任务ID
Ref: 审核记录.审核人 > 后台账号.账号ID

// 咨询管理
Ref: 咨询工单.用户 > 用户.用户ID
Ref: 咨询工单.接诊医生 > 后台账号.账号ID
Ref: 咨询工单.关联报告 > 体检报告.报告ID
Ref: 咨询回复.所属工单 > 咨询工单.咨询ID
Ref: 咨询回复.回复人 > 用户.用户ID

// 数据分析
Ref: 对比任务.用户 > 用户.用户ID
Ref: 对比任务.基线报告 > 体检报告.报告ID
Ref: 对比任务.对比报告 > 体检报告.报告ID
Ref: 对比结果.所属任务 > 对比任务.任务ID
Ref: 风险评分.关联任务 > 对比任务.任务ID
Ref: 风险评分.用户 > 用户.用户ID
Ref: 健康建议.用户 > 用户.用户ID
Ref: 健康建议.关联报告 > 体检报告.报告ID
Ref: 健康建议.关联任务 > 对比任务.任务ID
