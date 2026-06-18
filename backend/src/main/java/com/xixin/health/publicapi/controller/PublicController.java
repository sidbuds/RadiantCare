package com.xixin.health.publicapi.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.publicapi.service.PublicService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final PublicService publicService;

    public PublicController(PublicService publicService) {
        this.publicService = publicService;
    }

    @GetMapping("/packages")
    public ApiResult<?> listPackages() {
        return ApiResult.success(publicService.listPackages());
    }

    @GetMapping("/packages/{packageCode}")
    public ApiResult<?> getPackageDetail(@PathVariable String packageCode) {
        Object detail = publicService.getPackageDetail(packageCode);
        if (detail == null) {
            return ApiResult.fail(1002, "套餐不存在");
        }
        return ApiResult.success(detail);
    }

    @GetMapping("/centers")
    public ApiResult<?> listCenters() {
        return ApiResult.success(publicService.listCenters());
    }

    @GetMapping("/centers/{centerCode}")
    public ApiResult<?> getCenterDetail(@PathVariable String centerCode) {
        return ApiResult.success(publicService.getCenterDetail(centerCode));
    }

    @GetMapping("/centers/{centerCode}/slots")
    public ApiResult<?> getCenterSlots(
            @PathVariable String centerCode,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return ApiResult.success(publicService.getCenterSlots(centerCode, date));
    }

    @GetMapping("/content/checkup-guide")
    public ApiResult<?> getCheckupGuide() {
        return ApiResult.success(publicService.getCheckupGuide());
    }

    @GetMapping("/content/faq")
    public ApiResult<?> getFaq() {
        return ApiResult.success(publicService.getFaq());
    }
}
