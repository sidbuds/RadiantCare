package com.xixin.health.appointment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

@Data
@TableName("exam_package_item")
public class ExamPackageItemEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long packageId;
    private String itemCode;
    private String itemName;
    private String unit;
    private String refRange;
    private Integer sortNo;
}
