package com.xixin.health.operator.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class SavePackageRouteRequest {
    @NotBlank(message = "centerCode不能为空")
    private String centerCode;
    @Valid
    @NotEmpty(message = "routes不能为空")
    private List<RouteItem> routes;

    @Data
    public static class RouteItem {
        @NotBlank(message = "itemCode不能为空")
        private String itemCode;
        private String itemName;
        @NotBlank(message = "departmentCode不能为空")
        private String departmentCode;
        @NotBlank(message = "departmentName不能为空")
        private String departmentName;
        private String roomNo;
        private String floorNo;
        private String buildingNo;
        private Integer routeSort;
        private String guideText;
        private Integer needEmptyStomach;
        private Integer status;
    }
}
