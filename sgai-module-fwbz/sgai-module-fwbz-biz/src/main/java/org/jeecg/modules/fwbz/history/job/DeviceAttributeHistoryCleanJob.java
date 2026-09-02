package org.jeecg.modules.fwbz.history.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.history.service.IDeviceAttributeHistoryCleanService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 设备属性历史数据清理定时任务
 * <p>
 * 每天 00:00:01 执行一次：清除 device_attribute_history 表中 3 个月之前的历史数据，
 * 防止历史表无限膨胀。实际删除逻辑在 {@link IDeviceAttributeHistoryCleanService} 中分批执行，
 * 此处仅负责调度。
 * </p>
 *
 * @author fwbz
 */
@Slf4j
@Component
@AllArgsConstructor
public class DeviceAttributeHistoryCleanJob {

    /** 历史数据保留时长（月） */
    private static final int RETAIN_MONTHS = 3;

    private final IDeviceAttributeHistoryCleanService cleanService;

    /**
     * 每天 00:00:01 执行历史数据清理
     */
    @Scheduled(cron = "1 0 0 * * ?")
    public void cleanExpiredHistory() {
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(RETAIN_MONTHS);
        log.info("设备属性历史清理开始, 清理截止时间(早于该时间将被删除)={}", cutoff);
        try {
            int count = cleanService.cleanHistoryBefore(cutoff);
            log.info("设备属性历史清理完成, 共删除 {} 条", count);
        } catch (Exception e) {
            log.error("设备属性历史清理异常", e);
        }
    }
}
