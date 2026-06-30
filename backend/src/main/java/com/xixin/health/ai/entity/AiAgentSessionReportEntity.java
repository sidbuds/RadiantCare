package com.xixin.health.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

@Data
@TableName("ai_agent_session_report")
public class AiAgentSessionReportEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private String sessionNo;
    private Long reportId;
    private String reportNo;
}
