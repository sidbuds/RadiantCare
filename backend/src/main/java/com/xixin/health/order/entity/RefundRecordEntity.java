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
    private Long refundApplyId;
    private Long orderId;
    private String orderNo;
    private String channel;
    private String channelRefundNo;
    private BigDecimal refundAmount;
    private Integer status;
    private LocalDateTime refundTime;
    private String rawPayload;
}