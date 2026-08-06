package org.jeecg.modules.fwbz.energyAnalysis.dto;

import lombok.Data;

/**
 * 数据统计
 */
@Data
public class AlarmRecordStatisticsDto {

    /**
     * 待处理告警
     */
    private Long untreatedCount;

    /**
     * 处理中
     */
    private Long eventCount;

    /**
     * 今日已处理
     */
    private Long completedCount;

    /**
     * 平均处理时长(分钟)
     */
    private double averageProcessingTime;

}
