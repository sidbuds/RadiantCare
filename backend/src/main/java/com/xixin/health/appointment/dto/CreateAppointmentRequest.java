package com.xixin.health.appointment.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class CreateAppointmentRequest {
    @NotNull
    private Long packageId;
    @NotBlank
    private String centerCode;
    @NotNull
    private LocalDate appointDate;
    @NotBlank
    private String timeSlotCode;
    private String remark;
}
