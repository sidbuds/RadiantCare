package com.xixin.health.operator.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xixin.health.common.api.ApiResult;
import com.xixin.health.appointment.service.AppointmentService;
import com.xixin.health.common.model.PageResult;
import com.xixin.health.operator.service.OperatorAppointmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 运营端预约控制器
 */
@RestController
@RequestMapping("/api/operator/appointments")
@PreAuthorize("hasRole('OPERATOR')")
public class OperatorAppointmentController {

    private final OperatorAppointmentService operatorAppointmentService;

    private final AppointmentService appointmentService;

    public OperatorAppointmentController(OperatorAppointmentService operatorAppointmentService,
                                         AppointmentService appointmentService) {
        this.operatorAppointmentService = operatorAppointmentService;
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public ApiResult<?> list(@RequestParam(required = false) String centerCode,
                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate appointDate,
                             @RequestParam(required = false) Integer status,
                             @RequestParam(required = false) String mobile,
                             @RequestParam(defaultValue = "1") int pageNum,
                             @RequestParam(defaultValue = "10") int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<?> list = operatorAppointmentService.list(centerCode, appointDate, status, mobile);
        PageInfo<?> pageInfo = new PageInfo<>(list);
        return ApiResult.success(PageResult.of(list, pageInfo.getTotal(), pageNum, pageSize));
    }

    @GetMapping("/{appointmentNo}")
    public ApiResult<?> detail(@PathVariable String appointmentNo) {
        return ApiResult.success(operatorAppointmentService.detail(appointmentNo));
    }

    @PostMapping("/{appointmentNo}/cancel")
    public ApiResult<?> cancel(@PathVariable String appointmentNo,
                               @RequestParam(required = false) String reason) {
        operatorAppointmentService.cancel(appointmentNo, reason);
        return ApiResult.success();
    }

    @PostMapping("/mark-noshows")
    public ApiResult<?> markNoShows() {
        int count = appointmentService.markExpiredNoShows();
        return ApiResult.success(java.util.Collections.singletonMap("markedCount", count));
    }
}
