package org.jeecg.modules.fwbz.energyAnalysis.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 能源计量数据统计
 */
@Data
public class EnergyMeteringStatisticsDto {

    /**
     * 计量表计总数
     */
    private Long count;

    /**
     * 计量表计新增数
     */
    private String addCount;

    /**
     * 表计在线率
     */
    private String onlineRate;

    /**
     * 今日用电量
     */
    private BigDecimal electricCount;
    /**
     * 用电量环比
     */


    private String  electricCountDoD;



    /**
     * 今日用水量
     */
    private BigDecimal waterCount;




    /**
     * 用水量环比
     */


    private String  waterCountDoD;
}
