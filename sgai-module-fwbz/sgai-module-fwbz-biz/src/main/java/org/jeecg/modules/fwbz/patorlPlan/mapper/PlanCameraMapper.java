package org.jeecg.modules.fwbz.patorlPlan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.fwbz.patorlPlan.entity.PlanCamera;

import java.util.List;

/**
 * @Description: 巡更计划关联摄像头
 * @Author: jeecg-boot
 * @Date:   2026-07-31
 * @Version: V1.0
 */
public interface PlanCameraMapper extends BaseMapper<PlanCamera> {

    /**
     * 根据计划ID删除关联摄像头
     */
    int deleteByPlanId(@Param("planId") Long planId);

    /**
     * 根据计划ID查询关联摄像头
     */
    List<PlanCamera> selectByPlanId(@Param("planId") Long planId);
}
