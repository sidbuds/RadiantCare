package com.xixin.health.exam.dto;

import lombok.Data;

/**
 * 导检引导项 VO - 合并体检任务项与路线引导信息
 */
@Data
public class GuideItemVO {
    // 任务项信息
    private Long taskItemId;
    private String taskItemNo;
    private String itemCode;
    private String itemName;
    private String doctorName;
    private Integer itemStatus;

    // 路线引导信息
    private String departmentCode;
    private String departmentName;
    private String roomNo;
    private String floorNo;
    private String buildingNo;
    private String guideText;
    private Integer needEmptyStomach;
    private Integer routeSort;
}
