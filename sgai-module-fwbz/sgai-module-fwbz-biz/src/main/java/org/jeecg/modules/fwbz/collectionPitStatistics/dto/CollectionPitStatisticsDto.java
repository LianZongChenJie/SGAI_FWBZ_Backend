package org.jeecg.modules.fwbz.collectionPitStatistics.dto;

import lombok.Data;

/**
 * 集水坑统计
 */
@Data
public class CollectionPitStatisticsDto {

    /**
     * 液位告警设备数
     */
    private Long liquidLevelAlarmCount;

    /**
     * 故障设备总数
     */
    private Long faultDeviceCount;

}
