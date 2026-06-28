package com.xixin.health.appointment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 体检套餐实体
 */
@Data
@TableName("exam_package")
public class ExamPackageEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    //
    private String packageCode;
    private String packageName;
    private String category;
    private BigDecimal price;
    private Integer status;
    private String remark;
}
