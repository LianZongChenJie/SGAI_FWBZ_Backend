package org.jeecg.modules.fwbz.energyAnalysis.dto;

import lombok.Data;

import java.util.Map;

/**
 * 数据统计
 */
@Data
public class AlarmRecordStatisticsDto {


    /**
     * 告警总数
     */
    private Long count;

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

    /**
     * 报警类别分类统计
     */
    private Map<String, Long> categoryIdMap;

    /**
     * 严重数量
     */
    private Long seriousCount;




}
