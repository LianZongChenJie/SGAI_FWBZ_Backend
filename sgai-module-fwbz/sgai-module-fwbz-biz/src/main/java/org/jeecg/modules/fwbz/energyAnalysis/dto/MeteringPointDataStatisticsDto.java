package org.jeecg.modules.fwbz.energyAnalysis.dto;

import lombok.Data;

/**
 * 计量分析数据统计
 */
@Data
public class MeteringPointDataStatisticsDto {

    /**
     * 本月总耗电
     */
    private Long electricCount;

    /**
     * 本月总耗水
     */
    private Long waterCount;

    /**
     * 日均耗电
     */
    private Long electricAvg;

    /**
     * 环比节能
     */
    private Double mom;
}
