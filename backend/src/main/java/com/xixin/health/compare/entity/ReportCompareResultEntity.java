package com.xixin.health.compare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("report_compare_result")
public class ReportCompareResultEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String itemCode;
    private String itemName;
    private String baseValue;
    private String compareValue;
    private BigDecimal changeValue;
    private BigDecimal changeRate;
    private Integer trend;
}
