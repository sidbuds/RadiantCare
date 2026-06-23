package com.xixin.health.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

@Data
@TableName("health_profile")
public class HealthProfileEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String allergyHistory;
    private String medicalHistory;
    private String familyHistory;
    private String medicationHistory;
    private Integer smokingStatus;
    private Integer drinkingStatus;
    private String remark;
}
