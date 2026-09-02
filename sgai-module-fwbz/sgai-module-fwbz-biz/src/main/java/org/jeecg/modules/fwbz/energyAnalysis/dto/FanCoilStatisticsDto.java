package org.jeecg.modules.fwbz.energyAnalysis.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 风机盘管系统数据统计
 */
@Data
public class FanCoilStatisticsDto {

    /**
     * 风机盘管总数
     */
    private Long count;

    /**
     * 运行中
     */
    private Long online;

    /**
     * 今日能耗
     */
    private BigDecimal energyConsumption;

}
