package com.xixin.health.operator.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class SaveScheduleRequest {
    @NotBlank(message = "centerCode cannot be blank")
    private String centerCode;
    @NotNull(message = "appointDate cannot be null")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate appointDate;
    @NotBlank(message = "timeSlotCode cannot be blank")
    private String timeSlotCode;
    private String resourceType;
    private String resourceCode;
    @NotNull(message = "capacityTotal cannot be null")
    @Min(value = 0, message = "capacityTotal cannot be less than 0")
    private Integer capacityTotal;
    @NotNull(message = "status cannot be null")
    private Integer status;
    private String departmentCode;
    private String departmentName;
}
