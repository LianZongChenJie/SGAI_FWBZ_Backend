package org.jeecg.modules.fwbz.api;

import org.jeecg.modules.fwbz.api.fallback.FwbzDeviceFallback;
import org.jeecg.modules.fwbz.entity.DeviceAttributeEntity;
import org.jeecg.modules.fwbz.entity.DeviceEntity;
import org.jeecg.modules.fwbz.entity.DeviceInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "jeecg-gateway", fallbackFactory = FwbzDeviceFallback.class)
public interface FwbzDeviceApi {
    /**
     * 获取设备基本信息
     * @return 设备id、设备编码、设备类型
     */
    @GetMapping(value = "/fwbz/device/api/deviceList")
    List<DeviceEntity> deviceList();

    /**
     * 获取设备详细信息
     * @param deviceIds 设备id
     * @return 设备详细信息
     */
    @GetMapping(value = "/fwbz/device/api/deviceInfoList")
    List<DeviceInfo> deviceInfoList(@RequestParam String deviceIds);

    @GetMapping(value = "/fwbz/device/api/deviceAttributeList")
    List<DeviceAttributeEntity> deviceAttributeList();
}
