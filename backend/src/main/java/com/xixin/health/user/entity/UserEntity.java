package com.xixin.health.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class UserEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String userNo;
    private String name;
    private Integer gender;
    private String mobile;
    private String passwordHash;
    private Integer status;
    private LocalDateTime lastLoginAt;
}
