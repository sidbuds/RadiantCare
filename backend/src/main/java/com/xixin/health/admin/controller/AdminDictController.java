package com.xixin.health.admin.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.common.entity.DataDictionaryEntity;
import com.xixin.health.admin.service.AdminDictService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dicts")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDictController {

    private final AdminDictService adminDictService;

    public AdminDictController(AdminDictService adminDictService) {
        this.adminDictService = adminDictService;
    }

    @GetMapping("/types")
    public ApiResult<?> listTypes(@RequestParam(required = false) String keyword,
                                  @RequestParam(defaultValue = "1") int pageNum,
                                  @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.success(adminDictService.listDictTypes(keyword, pageNum, pageSize));
    }

    @GetMapping("/items")
    public ApiResult<?> listItems(@RequestParam String dictType,
                                  @RequestParam(required = false) Integer status,
                                  @RequestParam(required = false) String keyword) {
        return ApiResult.success(adminDictService.listDictItems(dictType, status, keyword));
    }

    @PostMapping("/items")
    public ApiResult<?> saveItem(@RequestBody DataDictionaryEntity entity) {
        adminDictService.saveDictItem(entity);
        return ApiResult.success(null);
    }

    @DeleteMapping("/items/{id}")
    public ApiResult<?> deleteItem(@PathVariable Long id) {
        adminDictService.deleteDictItem(id);
        return ApiResult.success(null);
    }
}
