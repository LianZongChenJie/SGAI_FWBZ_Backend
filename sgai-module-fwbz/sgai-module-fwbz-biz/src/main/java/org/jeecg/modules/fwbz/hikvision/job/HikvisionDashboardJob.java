package org.jeecg.modules.fwbz.hikvision.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.hikvision.service.IHikvisionDashboardTaskService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 海康看板数据同步定时任务
 * <p>每5分钟从海康API同步一次客流数据及人员统计数据，同步失败不抛异常，避免影响后续调度。</p>
 *
 * @author fwbz
 */
@Slf4j
@Component
@AllArgsConstructor
public class HikvisionDashboardJob {

    private final IHikvisionDashboardTaskService dashboardTaskService;

    /**
     * 每5分钟执行一次看板数据同步（客流 + 人员统计）
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void syncDashboardData() {
        log.debug("海康看板数据同步定时任务开始执行");
        try {
            dashboardTaskService.syncVisitorFlow();
            log.info("海康客流数据同步完成");
        } catch (Exception e) {
            log.error("海康客流数据同步异常", e);
        }
        try {
            dashboardTaskService.syncPersonRecognition();
            log.info("人员识别记录同步完成");
        } catch (Exception e) {
            log.error("人员识别记录同步异常", e);
        }
        try {
            dashboardTaskService.syncPersonnelStatistics();
            log.info("人员统计数据同步完成");
        } catch (Exception e) {
            log.error("人员统计数据同步异常", e);
        }
        log.info("海康看板数据同步定时任务结束");
    }
}
