package com.xixin.health.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

/**
 * 员工角色关联实体
 */
@Data
@TableName("staff_role_rel")
public class StaffRoleRelEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long staffAccountId;
    private String roleCode;
}
