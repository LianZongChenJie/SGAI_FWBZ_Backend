package org.jeecg.modules.fwbz.patorlPlan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.patorlPlan.dto.PatrolPlanDetailVo;
import org.jeecg.modules.fwbz.patorlPlan.dto.PatrolPlanDto;
import org.jeecg.modules.fwbz.patorlPlan.entity.PatrolPlan;

/**
 * @Description: 巡更计划
 * @Author: jeecg-boot
 * @Date:   2026-07-31
 * @Version: V1.0
 */
public interface IPatrolPlanService extends IService<PatrolPlan> {

    /**
     * 新增巡更计划（含关联摄像头）
     */
    void saveWithCameras(PatrolPlanDto dto);

    /**
     * 修改巡更计划（基本信息直接修改，关联摄像头先删后加）
     */
    void updateWithCameras(PatrolPlanDto dto);

    /**
     * 删除巡更计划（含关联摄像头）
     */
    void deleteWithCameras(Long id);

    /**
     * 查询巡更计划详情（含关联摄像头列表）
     */
    PatrolPlanDetailVo getDetail(Long id);

    /**
     * 获取正在运行中的巡更计划详情（staus=2，含关联摄像头列表）
     */
    PatrolPlanDetailVo getRunningPlanDetail();

    /**
     * 判断传入id是否为当前正在运行中的巡更计划
     */
    boolean isRunningPlan(Long id);
}
