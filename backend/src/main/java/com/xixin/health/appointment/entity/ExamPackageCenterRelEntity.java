package com.xixin.health.appointment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

@Data
@TableName("exam_package_center_rel")
public class ExamPackageCenterRelEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long packageId;
    private String centerCode;
    private String centerName;
    private Integer status;
}
