package org.jeecg.modules.fwbz.parkingStatistics.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.parkingStatistics.service.IParkingStatisticsService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 停车统计数据同步定时任务
 * <p>
 * 每5分钟从外部系统同步一次停车数据（今日进场/当前在场/剩余车位/平均停车时长）到数据库。
 * 同步失败不抛异常，避免影响后续调度。
 * </p>
 *
 * @author fwbz
 */
@Slf4j
@Component
@AllArgsConstructor
public class ParkingStatisticsJob {

    private final IParkingStatisticsService parkingStatisticsService;

    /**
     * 每5分钟执行一次停车数据同步
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void syncParkingStatisticsData() {
        log.debug("停车统计同步定时任务开始执行");
        try {
            parkingStatisticsService.syncAllFromApi();
            log.info("停车统计同步定时任务完成");
        } catch (Exception e) {
            log.error("停车统计同步定时任务异常", e);
        }
    }
}
