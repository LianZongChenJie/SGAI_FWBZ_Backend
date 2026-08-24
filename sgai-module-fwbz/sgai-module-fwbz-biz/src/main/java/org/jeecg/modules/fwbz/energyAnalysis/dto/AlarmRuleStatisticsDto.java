package org.jeecg.modules.fwbz.energyAnalysis.dto;

import lombok.Data;

/**
 * 数据统计
 */
@Data
public class AlarmRuleStatisticsDto {

    /**
     * 报警规则数
     */
    private Long count;

    /**
     * 启用规则
     */
    private Long enableCount;

    /**
     * 报警类型数
     */
    private Long categoryCount;

    /**
     * 报警等级数
     */
    private Long levelCount;

}
