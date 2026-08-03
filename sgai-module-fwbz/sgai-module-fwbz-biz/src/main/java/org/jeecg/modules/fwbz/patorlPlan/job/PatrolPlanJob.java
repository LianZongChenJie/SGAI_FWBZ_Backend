package org.jeecg.modules.fwbz.patorlPlan.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.patorlPlan.entity.PatrolPlan;
import org.jeecg.modules.fwbz.patorlPlan.mapper.PatrolHistoryMapper;
import org.jeecg.modules.fwbz.patorlPlan.mapper.PatrolPlanMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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
    private final PatrolHistoryMapper patrolHistoryMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

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

        // 3. 如果有到期的计划，设为运行中，并更新下次执行时间为明天
        if (planToRun != null) {
            String nextExecution = calcNextExecution(planToRun.getExecutionCycle());
            patrolPlanMapper.updateStatusToRunning(planToRun.getId(), nextExecution);

            // 4. 添加巡更历史记录（同一天同一计划只记录一次）
            int count = patrolHistoryMapper.countTodayByPlanId(planToRun.getId());
            if (count == 0) {
                patrolHistoryMapper.insertHistory(planToRun.getId());
                log.info("巡更计划定时任务：切换 [{}] (id={}) 为运行中，执行周期={}，下次执行={}，已添加历史记录",
                        planToRun.getPlanName(), planToRun.getId(), planToRun.getExecutionCycle(), nextExecution);
            } else {
                log.info("巡更计划定时任务：切换 [{}] (id={}) 为运行中，执行周期={}，下次执行={}，今日已有记录跳过",
                        planToRun.getPlanName(), planToRun.getId(), planToRun.getExecutionCycle(), nextExecution);
            }
        } else {
            log.debug("巡更计划定时任务：当前无到期计划，所有启用计划保持启用状态");
        }
    }

    /**
     * 计算下次执行时间：明天 + 执行周期时间
     */
    private String calcNextExecution(String executionCycle) {
        LocalTime time = LocalTime.parse(executionCycle, TIME_FORMATTER);
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        return tomorrow.format(DATE_FORMATTER) + " " + time.format(TIME_FORMATTER);
    }
}
