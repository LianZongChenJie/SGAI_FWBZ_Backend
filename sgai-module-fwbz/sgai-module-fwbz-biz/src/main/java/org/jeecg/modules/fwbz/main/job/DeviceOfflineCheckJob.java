package org.jeecg.modules.fwbz.main.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.mdm.constant.DeviceConstant;
import org.jeecg.modules.fwbz.mdm.entity.Device;
import org.jeecg.modules.fwbz.mdm.service.IDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备离线检测定时任务
 * <p>每5分钟执行一次：查询最后一次采集时间(last_gather_time)距今超过2小时的设备，
 * 将其运行状态(run_state)置为"离线"。</p>
 */
@Slf4j
@Component
public class DeviceOfflineCheckJob {

    @Autowired
    private IDeviceService deviceService;

    @Scheduled(cron = "0 */5 * * * ?")
    public void checkOfflineDevices() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(2);
        log.info("设备离线检测定时任务开始执行, 离线判定时间点={}", cutoff);
        try {
            // 最后一次采集时间早于 2小时前，且当前非"离线"状态的设备
            List<Device> list = deviceService.list(new LambdaQueryWrapper<Device>()
                    .isNotNull(Device::getLastGatherTime)
                    .lt(Device::getLastGatherTime, cutoff)
                    .ne(Device::getRunState, DeviceConstant.DEVICE_RUN_STATA_OFFLINE));
            if (list.isEmpty()) {
                log.info("设备离线检测完成, 无超时设备");
                return;
            }
            int count = 0;
            for (Device device : list) {
                try {
                    deviceService.updateStatus(device.getDeviceCode(), DeviceConstant.DEVICE_RUN_STATA_OFFLINE);
                    count++;
                } catch (Exception e) {
                    log.error("设备离线状态更新失败, deviceCode={}", device.getDeviceCode(), e);
                }
            }
            log.info("设备离线检测完成: 超时设备数={}, 已更新离线数={}", list.size(), count);
        } catch (Exception e) {
            log.error("设备离线检测定时任务执行异常", e);
        }
    }
}
