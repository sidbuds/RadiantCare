package com.xixin.health.compare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

/**
 * 指标趋势标签实体
 */
@Data
@TableName("indicator_trend_tag")
public class IndicatorTrendTagEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String itemCode;
    private String trendTag;
    private Integer trendDirection;
    private Integer turningYear;
    private Integer riskLevel;
    private String tagReason;
}
