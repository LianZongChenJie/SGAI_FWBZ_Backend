package org.jeecg.modules.fwbz.parkingStatistics.vo;

import lombok.Data;

/**
 * 24 小时停车流量 VO（用于"停车流量趋势"统计图）
 */
@Data
public class ParkingFlowStatVO {

    /**
     * 小时（0-23）
     */
    private Integer hour;

    /**
     * 进场车辆数
     */
    private Long inCount;

    /**
     * 出场车辆数
     */
    private Long outCount;
}
