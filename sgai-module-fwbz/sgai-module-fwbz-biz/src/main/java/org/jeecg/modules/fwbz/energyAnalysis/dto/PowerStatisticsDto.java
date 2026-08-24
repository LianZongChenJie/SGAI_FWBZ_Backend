package org.jeecg.modules.fwbz.energyAnalysis.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 配电系统数据统计
 */
@Data
public class PowerStatisticsDto {

    /**
     * 空调机组总数
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

    /**
     * 平均功率因数
     */
    private BigDecimal avgPowerFactor;

}
