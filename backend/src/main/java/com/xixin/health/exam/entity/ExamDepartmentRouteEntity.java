package com.xixin.health.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

@Data
@TableName("exam_department_route")
public class ExamDepartmentRouteEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String routeCode;
    private String centerCode;
    private Long packageId;
    private String itemCode;
    private String departmentCode;
    private String departmentName;
    private String roomNo;
    private String floorNo;
    private String buildingNo;
    private Integer routeSort;
    private String guideText;
    private Integer needEmptyStomach;
    private Integer status;
}
