package org.jeecg.modules.fwbz.energyAnalysis.vo;

import lombok.Data;
import org.jeecg.modules.fwbz.energyAnalysis.entity.CostCenterRel;

import java.math.BigDecimal;

@Data
public class ElectricityInTimePeriodVo {
    /**
     * 时间段
     */
    private String timePeriod;
    /**
     * 用电量
     */
    private BigDecimal electricity;
    /**
     * 占比
     */
    private String proportion;
    /**
     * 环比
     */
    private String MoM;


}
