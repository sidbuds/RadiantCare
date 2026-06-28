package com.xixin.health.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

@Data
@TableName("operation_log")
public class OperationLogEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long operatorId;
    private String operatorName;
    private String operatorType;
    private String module;
    private String action;
    private String targetType;
    private String targetId;
    private String ip;
    private String bizType;
    private String bizId;
    private String beforeData;
    private String afterData;
}
