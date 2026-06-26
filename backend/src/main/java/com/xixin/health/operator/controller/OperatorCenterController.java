package com.xixin.health.operator.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xixin.health.common.api.ApiResult;
import com.xixin.health.common.model.PageResult;
import com.xixin.health.operator.service.OperatorCenterService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/operator/centers")
@PreAuthorize("hasRole('OPERATOR')")
public class OperatorCenterController {

    private final OperatorCenterService operatorCenterService;

    public OperatorCenterController(OperatorCenterService operatorCenterService) {
        this.operatorCenterService = operatorCenterService;
    }

    @GetMapping
    public ApiResult<?> list(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) Integer status,
                             @RequestParam(defaultValue = "1") int pageNum,
                             @RequestParam(defaultValue = "10") int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<?> list = operatorCenterService.list(keyword, status);
        PageInfo<?> pageInfo = new PageInfo(list);
        return ApiResult.success(PageResult.of(list, pageInfo.getTotal(), pageNum, pageSize));
    }

    @PostMapping
    public ApiResult<?> create(@RequestBody Map<String, Object> body) {
        return ApiResult.success(operatorCenterService.create(body));
    }

    @PutMapping("/{id}")
    public ApiResult<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ApiResult.success(operatorCenterService.update(id, body));
    }

    @PatchMapping("/{id}/status")
    public ApiResult<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Number status = (Number) body.get("status");
        operatorCenterService.updateStatus(id, status == null ? null : status.intValue());
        return ApiResult.success(null);
    }
}
