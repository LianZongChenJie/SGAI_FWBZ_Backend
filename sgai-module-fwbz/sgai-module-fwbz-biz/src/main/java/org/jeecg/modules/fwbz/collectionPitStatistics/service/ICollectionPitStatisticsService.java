package org.jeecg.modules.fwbz.collectionPitStatistics.service;

import org.jeecg.modules.fwbz.collectionPitStatistics.dto.CollectionPitStatisticsDto;

/**
 * 集水坑统计 Service
 */
public interface ICollectionPitStatisticsService {

    /**
     * 集水坑统计（液位告警设备数、故障设备总数）
     *
     * @return 统计结果
     */
    CollectionPitStatisticsDto statistics();

}
