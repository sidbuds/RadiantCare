package com.xixin.health.compare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

/**
 * 报告对比任务实体
 */
@Data
@TableName("report_compare_task")
public class ReportCompareTaskEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskNo;
    private Long userId;
    private Long baselineReportId;
    private Long compareReportId;
    private Integer status;
}
