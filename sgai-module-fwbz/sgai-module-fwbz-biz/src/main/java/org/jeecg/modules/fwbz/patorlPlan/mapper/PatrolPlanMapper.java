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
     * 将指定巡更计划状态更新为运行中
     */
    int updateStatusToRunning(@Param("planId") Long planId);
}
