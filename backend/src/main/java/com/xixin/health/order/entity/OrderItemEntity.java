package com.xixin.health.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("order_item")
public class OrderItemEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Integer itemType;
    private String refItemCode;
    private String refItemName;
    private Integer qty;
    private BigDecimal unitPrice;
    private BigDecimal amount;
}
