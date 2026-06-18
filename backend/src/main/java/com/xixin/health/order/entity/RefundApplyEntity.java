package com.xixin.health.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("refund_apply")
public class RefundApplyEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String applyNo;
    private Long orderId;
    private String orderNo;
    private String refundType;
    private BigDecimal applyAmount;
    private String reason;
    private Integer applyStatus;
    private Integer auditStatus;
}