package org.jeecg.modules.fwbz.venueVisitorFlow.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.venueVisitorFlow.service.IVenueFlowService;
import org.jeecg.modules.fwbz.venueVisitorFlow.service.IVenueVisitorFlowService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 场馆客流数据同步定时任务
 * <p>每5分钟从 HTTP API 同步一次客流数据（整体客流 + 各场馆客流），同步失败不抛异常，避免影响后续调度。</p>
 *
 * @author fwbz
 */
@Slf4j
@Component
@AllArgsConstructor
public class VenueVisitorFlowJob {

    private final IVenueVisitorFlowService venueVisitorFlowService;
    private final IVenueFlowService venueFlowService;

    /**
     * 每5分钟执行一次客流数据同步（整体客流 + 各场馆客流）
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void syncAllFlowData() {
        try {
            int count = venueFlowService.syncAllVenueFlowFromApi();

        } catch (Exception e) {
            log.error("各场馆客流同步异常", e);
        }
        log.info("场馆客流同步定时任务执行完毕");
    }
}