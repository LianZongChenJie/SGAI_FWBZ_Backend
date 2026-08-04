package org.jeecg.modules.fwbz.parkingStatistics.dto;

import lombok.Data;

/**
 * 外部停车场车位分布单项 DTO
 * <p>
 * 示例：{ "name": "秀池地下停车场", "spaces": 855, "shengyu": 754, "state": "宽松", "saturation": 0.88, "usedRate": 0.12 }
 */
@Data
public class ExternalParkingSpaceItemDto {

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
     * 总车位数
     */
    private Long spaces;

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
     * 使用率
     */
    private Double usedRate;
}
