package com.xixin.health.appointment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

/**
 * 资源容量实体 - 时段预约名额管理
 */
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
    private String departmentCode;
    private String departmentName;
    private Integer capacityTotal;
    private Integer capacityUsed;
    private Integer capacityLocked;
    private Integer status;
    private Integer versionNo;
}
