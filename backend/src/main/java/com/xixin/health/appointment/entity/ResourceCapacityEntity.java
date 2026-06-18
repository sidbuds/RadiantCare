package com.xixin.health.appointment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

@Data
@TableName("resource_capacity")
public class ResourceCapacityEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String centerCode;
    private java.time.LocalDate appointDate;
    private String timeSlotCode;
    private String resourceType;
    private String resourceCode;
    private Integer capacityTotal;
    private Integer capacityUsed;
    private Integer capacityLocked;
    private Integer status;
    private Integer versionNo;
}
