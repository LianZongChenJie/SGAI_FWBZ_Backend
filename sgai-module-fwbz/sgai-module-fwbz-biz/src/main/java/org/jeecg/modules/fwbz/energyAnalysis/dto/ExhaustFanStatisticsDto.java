package org.jeecg.modules.fwbz.energyAnalysis.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 排风机系统数据统计
 */
@Data
public class ExhaustFanStatisticsDto {

    /**
     * 今日能耗
     */
    private BigDecimal energyConsumption;

    /**
     * 故障数
     */
    private Integer faultCount;
}
