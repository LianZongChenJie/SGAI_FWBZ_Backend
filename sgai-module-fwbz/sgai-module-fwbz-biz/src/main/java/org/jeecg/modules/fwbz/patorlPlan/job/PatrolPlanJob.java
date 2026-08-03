package org.jeecg.modules.fwbz.patorlPlan.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.patorlPlan.entity.PatrolPlan;
import org.jeecg.modules.fwbz.patorlPlan.mapper.PatrolPlanMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Description: 巡更计划定时任务 - 根据执行周期自动切换运行中状态
 * @Author: jeecg-boot
 * @Date:   2026-08-03
 * @Version: V1.0
 */
@Component
@AllArgsConstructor
@Slf4j
public class PatrolPlanJob {

    private final PatrolPlanMapper patrolPlanMapper;

    /**
     * 每30秒执行一次，根据执行周期将最近一个到期的巡更计划设为运行中，
     * 其他非停用计划重置为启用，保证同一时间只有一条计划在运行中。
     */
    @Scheduled(cron = "0/30 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void switchRunningStatus() {
        // 1. 查询到期的巡更计划（非停用，执行周期 <= 当前时间，取最近一个）
        PatrolPlan planToRun = patrolPlanMapper.selectPlanToRun();

        // 2. 先将所有非停用计划重置为启用
        patrolPlanMapper.resetNonDisabledStatus();

        // 3. 如果有到期的计划，设为运行中
        if (planToRun != null) {
            patrolPlanMapper.updateStatusToRunning(planToRun.getId());
            log.info("巡更计划定时任务：切换 [{}] (id={}) 为运行中，执行周期={}",
                    planToRun.getPlanName(), planToRun.getId(), planToRun.getExecutionCycle());
        } else {
            log.debug("巡更计划定时任务：当前无到期计划，所有启用计划保持启用状态");
        }
    }
}
