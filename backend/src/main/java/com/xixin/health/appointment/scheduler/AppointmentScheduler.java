package com.xixin.health.appointment.scheduler;

import com.xixin.health.appointment.service.AppointmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 预约定时任务
 */
@Slf4j
@Component
public class AppointmentScheduler {

    private final AppointmentService appointmentService;

    public AppointmentScheduler(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * 每天凌晨 00:05 自动标记爽约预约
     * 条件：status=1（待体检）且 appoint_date < 今天
     */
    @Scheduled(cron = "0 5 0 * * ?")
    public void markNoShows() {
        log.info("NoShow task start...");
        int count = appointmentService.markExpiredNoShows();
        log.info("NoShow task done, marked {} appointments", count);
    }
}
