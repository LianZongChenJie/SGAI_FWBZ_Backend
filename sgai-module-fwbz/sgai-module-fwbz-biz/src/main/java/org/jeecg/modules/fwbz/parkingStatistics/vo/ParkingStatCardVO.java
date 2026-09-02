package org.jeecg.modules.fwbz.parkingStatistics.vo;

import lombok.Data;

/**
 * 停车统计卡片VO
 */
@Data
public class ParkingStatCardVO {

    /**
     * 标题
     */
    private String title;

    /**
     * 数值
     */
    private Number value;

    /**
     * 单位/后缀（如 h、%）
     */
    private String unit;

    /**
     * 上下文描述（如 ↑9.3% 较昨日、可用）
     */
    private String context;
}
