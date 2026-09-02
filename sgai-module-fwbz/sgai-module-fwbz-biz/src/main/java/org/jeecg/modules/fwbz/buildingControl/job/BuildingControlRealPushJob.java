package org.jeecg.modules.fwbz.buildingControl.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.buildingControl.service.BuildingControlRealPushService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 楼控系统读点定时任务（每 15 分钟）：
 * <p>
 * 自动读取楼控设备属性：按检测点ID(tagId)读取点位值，
 * 更新 device_attribute 设备属性表，并同步更新设备在线状态与最后采集时间。
 * 具体业务逻辑见 {@link BuildingControlRealPushService#readRealDataOnce()}。
 * <p>
 * cron 错峰：冷源历史保存 0/15 整点、冷源属性采集 2/15，本任务 1/15 执行，
 * 避免多个任务同时读取 pSpace SDK。
 * 整体异常由方法内兜底，不抛出到 Spring 调度器（避免影响后续周期）。
 *
 * @author fwbz
 */
@Slf4j
@Component
@AllArgsConstructor
public class BuildingControlRealPushJob {

    private final BuildingControlRealPushService buildingControlRealPushService;

    /**
     * 每 15 分钟执行一次楼控读点（cron 秒=0 分钟=1/15，错峰执行避免与冷源任务同时读 SDK）
     */
    @Scheduled(cron = "0 1/15 * * * ?")
    public void readRealData() {
        log.info("楼控读点定时任务开始");
        try {
            buildingControlRealPushService.readRealDataOnce();
            log.info("楼控读点定时任务完成");
        } catch (Exception e) {
            log.error("楼控读点定时任务异常", e);
        }
    }
}
