package com.xixin.health.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("refund_record")
public class RefundRecordEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String refundNo;
    private String applyNo;
    private String orderNo;
    private BigDecimal refundAmount;
    private Integer refundStatus;
    private LocalDateTime refundTime;
    private String thirdPartyNo;
    private String remark;
}