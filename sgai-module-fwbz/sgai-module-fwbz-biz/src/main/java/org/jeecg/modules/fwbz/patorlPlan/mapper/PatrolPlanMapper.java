package org.jeecg.modules.fwbz.patorlPlan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.fwbz.patorlPlan.entity.PatrolPlan;
import org.apache.ibatis.annotations.Param;

/**
 * @Description: 巡更计划
 * @Author: jeecg-boot
 * @Date:   2026-07-31
 * @Version: V1.0
 */
@Mapper
public interface PatrolPlanMapper extends BaseMapper<PatrolPlan> {

    /**
     * 获取正在运行中的巡更计划（staus = 2）
     */
    PatrolPlan selectRunningPlan();

    /**
     * 获取当前应切换为运行中的巡更计划（非停用且执行时间已到达的最近一个）
     */
    PatrolPlan selectPlanToRun();

    /**
     * 将所有非停用巡更计划状态重置为启用
     */
    int resetNonDisabledStatus();

    /**
     * 将指定巡更计划状态更新为运行中，同时更新下次执行时间
     */
    int updateStatusToRunning(@Param("planId") Long planId, @Param("nextExecution") String nextExecution);

    /**
     * 统计相同执行周期且非停用的巡更计划数量
     * @param executionCycle 执行周期
     * @param excludeId 排除的计划ID（编辑时排除自身）
     */
    int countByExecutionCycle(@Param("executionCycle") String executionCycle, @Param("excludeId") Long excludeId);
}
