package org.jeecg.modules.fwbz.parkingStatistics.dto;

import lombok.Data;

/**
 * 外部停车统计API返回数据DTO
 */
@Data
public class ExternalParkingStatDto {

    /**
     * 今日进场车辆数
     */
    private Long todayEntryCount;

    /**
     * 较昨日进场车辆变化率（示例 9.3 表示 9.3%）
     */
    private Double todayEntryChangeRate;

    /**
     * 当前在场车辆数
     */
    private Long currentInCount;

    /**
     * 较昨日在场车辆变化率（示例 -5.1 表示下降 5.1%）
     */
    private Double currentInChangeRate;

    /**
     * 剩余车位数
     */
    private Long remainingSpaceCount;

    /**
     * 剩余车位可用率（示例 12 表示 12%）
     */
    private Double remainingSpaceRate;

    /**
     * 平均停车时长（小时）
     */
    private Double averageParkingDuration;

    /**
     * 较昨日平均停车时长变化（小时）
     */
    private Double averageDurationChange;
}
