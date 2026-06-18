package com.xixin.health.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("staff_account")
public class StaffAccountEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String passwordHash;
    private String displayName;
    private Long bindUserId;
    private Integer status;
    private LocalDateTime lastLoginAt;
}
