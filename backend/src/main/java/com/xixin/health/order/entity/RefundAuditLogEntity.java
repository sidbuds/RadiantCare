package com.xixin.health.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

/**
 * 退款审核日志实体
 */
@Data
@TableName("refund_audit_log")
public class RefundAuditLogEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String applyNo;
    private Long auditorId;
    private String auditAction;
    private String auditRemark;
}