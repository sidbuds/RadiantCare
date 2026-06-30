package com.xixin.health.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

@Data
@TableName("ai_agent_session")
public class AiAgentSessionEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String sessionNo;
    private Long userId;
    private Integer status;
}
