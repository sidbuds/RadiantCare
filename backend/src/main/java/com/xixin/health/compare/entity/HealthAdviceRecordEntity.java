package com.xixin.health.compare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

/**
 * 健康建议记录实体
 */
@Data
@TableName("health_advice_record")
public class HealthAdviceRecordEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String adviceNo;
    private Long userId;
    private Long reportId;
    private Long compareTaskId;
    private String sourceType;
    private String adviceType;
    private Integer riskLevel;
    private String adviceTitle;
    private String adviceContent;
    private String actionSuggestion;
    private Integer status;
}
