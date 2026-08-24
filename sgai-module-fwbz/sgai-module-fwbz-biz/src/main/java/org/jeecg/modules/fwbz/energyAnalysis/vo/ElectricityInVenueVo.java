package org.jeecg.modules.fwbz.energyAnalysis.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ElectricityInVenueVo {
    /**
     * 名称
     */
    private String name;
    /**
     * 用电量
     */
    private BigDecimal electricity;

    /**
     * 用电量环比
     *
     */
    private String electricityMoM;

    /**
     * 用电占比
     */
    private String electricityProportion;

    /**
     * 用水量
     */
    private BigDecimal water;

    /**
     * 用水量环比
     */
    private String waterMoM;


}
