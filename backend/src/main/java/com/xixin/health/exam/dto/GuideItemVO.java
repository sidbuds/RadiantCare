package com.xixin.health.exam.dto;

import lombok.Data;

@Data
public class GuideItemVO {
    private Long taskItemId;
    private String taskItemNo;
    private String itemCode;
    private String itemName;
    private String doctorName;
    private Integer itemStatus;
    private String departmentCode;
    private String departmentName;
    private String roomNo;
    private String floorNo;
    private String buildingNo;
    private String guideText;
    private Integer needEmptyStomach;
    private Integer routeSort;
}
