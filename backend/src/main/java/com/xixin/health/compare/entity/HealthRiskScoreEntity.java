package com.xixin.health.compare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 健康风险评分实体
 */
@Data
@TableName("health_risk_score")
public class HealthRiskScoreEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long userId;
    private BigDecimal scoreTotal;
    private BigDecimal scoreAbnormal;
    private BigDecimal scoreTrend;
    private Integer riskLevel;
    private String scoreDetail;
}
