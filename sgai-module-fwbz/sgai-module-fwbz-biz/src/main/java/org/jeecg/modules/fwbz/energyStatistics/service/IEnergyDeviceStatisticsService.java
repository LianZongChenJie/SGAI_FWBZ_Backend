package org.jeecg.modules.fwbz.energyStatistics.service;

import org.jeecg.modules.fwbz.mdm.dto.DeviceRunStateStatisticsDto;

/**
 * 能耗设备统计服务
 */
public interface IEnergyDeviceStatisticsService {

    /**
     * 根据设备类别统计设备总数与在线数
     *
     * @param categoryId 设备类别id；为空时统计全部
     * @return 统计结果（count 总数、online 在线数、offline 离线数）
     */
    DeviceRunStateStatisticsDto statisticsByCategoryId(Long categoryId);
}
