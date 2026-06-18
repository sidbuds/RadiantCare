package com.xixin.health.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

@Data
@TableName("report_template")
public class ReportTemplateEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String templateCode;
    private String templateName;
    private Long packageId;
    private String templateType;
    private Integer versionNo;
    private String renderEngine;
    private String templateConfig;
    private Integer status;
}
