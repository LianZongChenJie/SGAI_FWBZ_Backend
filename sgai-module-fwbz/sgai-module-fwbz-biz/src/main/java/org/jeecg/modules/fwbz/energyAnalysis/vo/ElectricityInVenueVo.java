package org.jeecg.modules.fwbz.energyAnalysis.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ElectricityInVenueVo {
    /**
     * 时间段
     */
    private String venue;
    /**
     * 用电量
     */
    private BigDecimal electricity;

    /**
     * 用电量环比
     *
     */
    private BigDecimal electricityMoM;

    /**
     * 用电占比
     */
    private BigDecimal electricityProportion;

    /**
     * 用水量
     */
    private BigDecimal water;

    /**
     * 用水量环比
     */
    private BigDecimal waterMoM;


}
