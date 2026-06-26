package com.xixin.health.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

/**
 * 医生科室关联实体
 */
@Data
@TableName("doctor_department_rel")
public class DoctorDepartmentRelEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long doctorId;
    private String departmentCode;
    private String departmentName;
    private String centerCode;
    private Integer isPrimary;
}
