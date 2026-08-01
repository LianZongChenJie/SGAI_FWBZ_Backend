package org.jeecg.modules.fwbz.energyAnalysis.dto;

import lombok.Data;

/**
 * 计量规则数据统计
 */
@Data
public class MeteringPointStatisticsDto {

    /**
     * 计量项目总数
     */
    private Long count;

    /**
     * 已配置公式
     */
    private Long formulaCount;

    /**
     * 电表项目
     */
    private Long electricCount;

    /**
     * 水表项目
     */
    private Long waterCount;
}
