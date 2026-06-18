package com.xixin.health.appointment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("appointment")
public class AppointmentEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String appointmentNo;
    private Long userId;
    private Long packageId;
    private String centerCode;
    private LocalDate appointDate;
    private String timeSlotCode;
    private Integer status;
    private String cancelReason;
    private String remark;
}
