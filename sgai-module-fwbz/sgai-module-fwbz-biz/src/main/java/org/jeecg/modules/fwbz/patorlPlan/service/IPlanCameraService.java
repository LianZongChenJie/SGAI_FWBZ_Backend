package org.jeecg.modules.fwbz.patorlPlan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.patorlPlan.entity.PlanCamera;

import java.util.List;

/**
 * @Description: 巡更计划关联摄像头
 * @Author: jeecg-boot
 * @Date:   2026-07-31
 * @Version: V1.0
 */
public interface IPlanCameraService extends IService<PlanCamera> {

    /**
     * 根据计划ID删除关联摄像头
     */
    void deleteByPlanId(Long planId);

    /**
     * 批量保存关联摄像头
     */
    void batchSave(Long planId, List<String> indexCodes);

    /**
     * 根据计划ID查询关联摄像头
     */
    List<PlanCamera> selectByPlanId(Long planId);
}
