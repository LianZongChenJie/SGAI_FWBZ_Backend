package org.jeecg.modules.fwbz.parkingStatistics.dto;

import lombok.Data;

/**
 * 外部停车场车位分布单项 DTO
 * <p>
 * 示例：{ "name": "P1", "used": 156, "total": 300 }
 */
@Data
public class ExternalParkingSpaceItemDto {

    /**
     * 停车场名称（如 P1、P2）
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
}
