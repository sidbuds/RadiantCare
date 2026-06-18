package com.xixin.health.operator.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class SaveScheduleRequest {
    @NotBlank(message = "centerCode不能为空")
    private String centerCode;
    @NotNull(message = "appointDate不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate appointDate;
    @NotBlank(message = "timeSlotCode不能为空")
    private String timeSlotCode;
    @NotBlank(message = "resourceType不能为空")
    private String resourceType;
    @NotBlank(message = "resourceCode不能为空")
    private String resourceCode;
    @NotNull(message = "capacityTotal不能为空")
    @Min(value = 0, message = "capacityTotal不能小于0")
    private Integer capacityTotal;
    @NotNull(message = "status不能为空")
    private Integer status;
}
