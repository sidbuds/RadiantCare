package com.xixin.health.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

/**
 * 报告章节模板实体
 */
@Data
@TableName("report_section_template")
public class ReportSectionTemplateEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long templateId;
    private String sectionCode;
    private String sectionName;
    private String sectionType;
    private String dataSourceType;
    private String itemCodes;
    private Integer sortNo;
    private String renderRule;
}
