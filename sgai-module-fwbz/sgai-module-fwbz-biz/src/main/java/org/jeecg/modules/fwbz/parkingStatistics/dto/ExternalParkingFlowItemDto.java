package org.jeecg.modules.fwbz.parkingStatistics.dto;

import lombok.Data;

/**
 * 外部 24 小时停车流量 DTO
 * <p>
 * 示例：{ "hour": 8, "inCount": 23, "outCount": 18 }
 */
@Data
public class ExternalParkingFlowItemDto {

    /**
     * 小时（0-23）
     */
    private Integer hour;

    /**
     * 该小时进场车辆数
     */
    private Long inCount;

    /**
     * 该小时出场车辆数
     */
    private Long outCount;
}
