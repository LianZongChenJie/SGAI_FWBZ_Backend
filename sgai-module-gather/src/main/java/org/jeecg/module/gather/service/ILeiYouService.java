package org.jeecg.module.gather.service;

import org.jeecg.module.gather.dto.DeviceCommStatus;
import org.jeecg.module.gather.dto.DeviceData;
import org.jeecg.module.gather.dto.PointData;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ILeiYouService {

    void refreshToken();
    /**
     * 获取设备最新采集值
     * @param deviceCodeList 设备编号列表
     */
    List<DeviceData> getDeviceData(Collection<String> deviceCodeList);

    /**
     * 获取设备状态
     * @param deviceCodeList 设备编号
     */
    List<DeviceCommStatus> getOnlineData(Collection<String> deviceCodeList);

    List<PointData> getHistoryDeviceData(String deviceCode, String identifiers, LocalDateTime start, LocalDateTime end);
}
