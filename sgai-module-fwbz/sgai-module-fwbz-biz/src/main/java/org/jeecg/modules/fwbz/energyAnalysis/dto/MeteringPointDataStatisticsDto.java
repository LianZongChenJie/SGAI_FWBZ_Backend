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
     *   环比
     */
    private String electricCountMoM;

    /**
     * 本月总耗水
     */
    private Long waterCount;

    /**
     * 本月总耗水 环比
     */
    private String waterCountMoM;

    /**
     * 日均耗电
     */
    private Long electricAvg;

    /**
     * 日均耗电
     */
    private String electricAvgMom;

    /**
     * 环比节能
     */
    private String energySaving;

    /**
     * 环比节能 环比
     */
    private String energySavingMom;
}
