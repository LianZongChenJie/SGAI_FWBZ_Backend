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
     * 计量项目总数新增
     */
    private String addCount;

    /**
     * 已配置公式
     */
    private Long formulaCount;

    /**
     * 覆盖率
     */
    private String coverage;



    /**
     * 电表项目
     */
    private Long electricCount;

    /**
     * 水表项目
     */
    private Long waterCount;


    /**
     * 电表占比
     */
    private String electricPercentage;



    /**
     * 水表占比
     */
    private String waterPercentage;




}
