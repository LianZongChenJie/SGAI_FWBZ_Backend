package org.jeecg.modules.fwbz.parkingStatistics.vo;

import lombok.Data;

/**
 * 停车场实时车位分布 VO（用于"停车场实时状态"统计图）
 */
@Data
public class ParkingSpaceStatVO {

    /**
     * 停车场ID
     */
    private Long id;

    /**
     * 停车场名称
     */
    private String name;

    /**
     * 经度
     */
    private Double lng;

    /**
     * 纬度
     */
    private Double lat;

    /**
     * 已用车位数
     */
    private Long used;

    /**
     * 总车位数
     */
    private Long total;

    /**
     * 剩余车位数
     */
    private Long shengyu;

    /**
     * 车位状态（宽松/适中/拥挤）
     */
    private String state;

    /**
     * 饱和度
     */
    private Double saturation;

    /**
     * 使用率（百分比，保留 1 位小数）
     */
    private Double usageRate;

    /**
     * 使用率（原始值，来自API）
     */
    private Double usedRate;
}
