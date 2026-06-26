package com.xixin.health.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 报告检查项实体
 */
@Data
@TableName("exam_report_item")
public class ExamReportItemEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reportId;
    private String itemCode;
    private String itemName;
    private String resultValue;
    private BigDecimal resultNumber;
    private String unit;
    private String refRange;
    private Integer isAbnormal;
    private Integer abnormalLevel;
    private Integer sortNo;
}
