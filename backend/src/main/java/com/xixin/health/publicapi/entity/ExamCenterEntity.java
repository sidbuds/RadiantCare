package com.xixin.health.publicapi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

@Data
@TableName("exam_center")
public class ExamCenterEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String centerCode;
    private String centerName;
    private String address;
    private String phone;
    private String businessHours;
    private String description;
    private Integer status;
}
