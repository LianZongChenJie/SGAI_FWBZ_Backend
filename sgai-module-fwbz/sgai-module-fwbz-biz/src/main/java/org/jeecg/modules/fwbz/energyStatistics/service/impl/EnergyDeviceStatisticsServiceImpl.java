package org.jeecg.modules.fwbz.energyStatistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.mdm.constant.DeviceConstant;
import org.jeecg.modules.fwbz.mdm.dto.DeviceRunStateStatisticsDto;
import org.jeecg.modules.fwbz.mdm.entity.Device;
import org.jeecg.modules.fwbz.mdm.service.IDeviceService;
import org.jeecg.modules.fwbz.energyStatistics.service.IEnergyDeviceStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 能耗设备统计服务实现
 */
@Slf4j
@Service
public class EnergyDeviceStatisticsServiceImpl implements IEnergyDeviceStatisticsService {

    @Autowired
    private IDeviceService deviceService;

    @Override
    public DeviceRunStateStatisticsDto statisticsByCategoryId(Long categoryId) {
        List<Device> list = deviceService.list(new LambdaQueryWrapper<Device>()
                .select(Device::getId, Device::getRunState)
                .eq(categoryId != null, Device::getCategoryId, categoryId));
        Map<String, Long> runStateMap = list.stream()
                .filter(item -> item.getRunState() != null)
                .collect(Collectors.groupingBy(Device::getRunState, Collectors.counting()));
        DeviceRunStateStatisticsDto dto = new DeviceRunStateStatisticsDto();
        dto.setCount((long) list.size());
        dto.setOnline(runStateMap.getOrDefault(DeviceConstant.DEVICE_RUN_STATA_ONLINE, 0L));
        dto.setOffline(runStateMap.getOrDefault(DeviceConstant.DEVICE_RUN_STATA_OFFLINE, 0L));
        return dto;
    }
}
