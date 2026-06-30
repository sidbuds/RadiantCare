package com.xixin.health.ai.controller;

import com.xixin.health.ai.dto.ReportAgentChatRequest;
import com.xixin.health.ai.dto.ReportAgentSendReportRequest;
import com.xixin.health.ai.service.ReportAgentService;
import com.xixin.health.common.api.ApiResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/report-agent")
@PreAuthorize("hasRole('USER')")
public class ReportAgentController {

    private final ReportAgentService reportAgentService;

    public ReportAgentController(ReportAgentService reportAgentService) {
        this.reportAgentService = reportAgentService;
    }

    @PostMapping("/sessions")
    public ApiResult<?> createSession() {
        return ApiResult.success(reportAgentService.createSession());
    }

    @GetMapping("/sessions/current")
    public ApiResult<?> currentSession() {
        return ApiResult.success(reportAgentService.currentSession());
    }

    @PostMapping("/sessions/{sessionNo}/reports")
    public ApiResult<?> sendReport(@PathVariable String sessionNo,
                                   @Validated @RequestBody ReportAgentSendReportRequest request) {
        return ApiResult.success(reportAgentService.sendReport(sessionNo, request));
    }

    @PostMapping("/sessions/{sessionNo}/messages")
    public ApiResult<?> chat(@PathVariable String sessionNo,
                             @Validated @RequestBody ReportAgentChatRequest request) {
        return ApiResult.success(reportAgentService.chat(sessionNo, request));
    }

    @DeleteMapping("/sessions/{sessionNo}/reports/{reportNo}")
    public ApiResult<?> revokeReport(@PathVariable String sessionNo,
                                     @PathVariable String reportNo) {
        reportAgentService.revokeReport(sessionNo, reportNo);
        return ApiResult.success(null);
    }
}
