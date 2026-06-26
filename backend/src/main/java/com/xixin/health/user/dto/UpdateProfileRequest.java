package com.xixin.health.user.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UpdateProfileRequest {
    @NotBlank(message = "姓名不能为空")
    @Size(max = 64, message = "姓名最长64字符")
    private String name;
    private Integer gender;
    private String birthDate;
    @Size(max = 128, message = "邮箱最长128字符")
    private String email;
    @Size(max = 256, message = "地址最长256字符")
    private String address;
    @Size(max = 64, message = "紧急联系人最长64字符")
    private String emergencyContact;
    @Size(max = 32, message = "紧急联系电话最长32字符")
    private String emergencyMobile;
}
