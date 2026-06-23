package com.xixin.health.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class UserEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String userNo;
    private String name;
    private Integer gender;
    private LocalDate birthDate;
    private Integer idType;
    private String idNo;
    private String mobile;
    private String email;
    private String passwordHash;
    private String address;
    private String emergencyContact;
    private String emergencyMobile;
    private Integer status;
    private LocalDateTime lastLoginAt;
}
