package org.jeecg.modules.fwbz.venueVisitorFlow.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.venueVisitorFlow.service.IVenueVisitorFlowService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 场馆客流数据同步定时任务
 * <p>
 * 每5分钟从海康同步一次客流数据（今日总客流/当前在场/峰值客流/平均停留）到数据库。
 * 同步失败不抛异常，避免影响后续调度。
 * </p>
 *
 * @author fwbz
 */
@Slf4j
@Component
@AllArgsConstructor
public class VenueVisitorFlowJob {

    private final IVenueVisitorFlowService venueVisitorFlowService;

    /**
     * 每5分钟执行一次海康客流数据同步
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void syncVisitorFlowData() {
        log.debug("场馆客流同步定时任务开始执行");
        try {
            int count = venueVisitorFlowService.syncFromHikvision();
            log.info("场馆客流同步定时任务完成，成功同步 {} 项", count);
        } catch (Exception e) {
            log.error("场馆客流同步定时任务异常", e);
        }
    }
}
