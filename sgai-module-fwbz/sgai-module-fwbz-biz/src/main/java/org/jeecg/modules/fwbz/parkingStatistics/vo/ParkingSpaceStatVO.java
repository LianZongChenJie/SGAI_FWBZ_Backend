package org.jeecg.modules.fwbz.parkingStatistics.vo;

import lombok.Data;

/**
 * 停车场实时车位分布 VO（用于"停车场实时状态"统计图）
 */
@Data
public class ParkingSpaceStatVO {

    /**
     * 停车场名称（如 P1）
     */
    private String name;

    /**
     * 已用车位数
     */
    private Long used;

    /**
     * 总车位数
     */
    private Long total;

    /**
     * 使用率（百分比，保留 1 位小数）
     */
    private Double usageRate;
}
