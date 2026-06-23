package com.xixin.health.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment_record")
public class PaymentRecordEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String paymentNo;
    private String orderNo;
    private Long userId;
    private String payMethod;
    private BigDecimal payAmount;
    private Integer payStatus;
    private LocalDateTime payTime;
    private String thirdPartyNo;
    private String remark;
}
