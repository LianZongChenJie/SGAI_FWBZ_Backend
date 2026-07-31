package org.jeecg.modules.fwbz.patorlPlan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.fwbz.patorlPlan.dto.PatrolPlanDetailVo;
import org.jeecg.modules.fwbz.patorlPlan.dto.PatrolPlanDto;
import org.jeecg.modules.fwbz.patorlPlan.entity.PatrolPlan;
import org.jeecg.modules.fwbz.patorlPlan.entity.PlanCamera;
import org.jeecg.modules.fwbz.patorlPlan.mapper.PatrolPlanMapper;
import org.jeecg.modules.fwbz.patorlPlan.service.IPatrolPlanService;
import org.jeecg.modules.fwbz.patorlPlan.service.IPlanCameraService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Description: 巡更计划
 * @Author: jeecg-boot
 * @Date:   2026-07-31
 * @Version: V1.0
 */
@Service
public class PatrolPlanServiceImpl extends ServiceImpl<PatrolPlanMapper, PatrolPlan> implements IPatrolPlanService {

    @Autowired
    private IPlanCameraService planCameraService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithCameras(PatrolPlanDto dto) {
        // 1. 保存巡更计划基本信息
        PatrolPlan patrolPlan = new PatrolPlan();
        BeanUtils.copyProperties(dto, patrolPlan);
        this.save(patrolPlan);

        // 2. 保存关联摄像头
        if (dto.getIndexCodes() != null && !dto.getIndexCodes().isEmpty()) {
            planCameraService.batchSave(patrolPlan.getId(), dto.getIndexCodes());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWithCameras(PatrolPlanDto dto) {
        // 1. 修改巡更计划基本信息
        PatrolPlan patrolPlan = new PatrolPlan();
        BeanUtils.copyProperties(dto, patrolPlan);
        this.updateById(patrolPlan);

        // 2. 关联摄像头：先删除后添加
        planCameraService.deleteByPlanId(dto.getId());
        if (dto.getIndexCodes() != null && !dto.getIndexCodes().isEmpty()) {
            planCameraService.batchSave(dto.getId(), dto.getIndexCodes());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWithCameras(Long id) {
        // 1. 删除关联摄像头
        planCameraService.deleteByPlanId(id);
        // 2. 删除巡更计划
        this.removeById(id);
    }

    @Override
    public PatrolPlanDetailVo getDetail(Long id) {
        PatrolPlan patrolPlan = this.getById(id);
        if (patrolPlan == null) {
            return null;
        }
        PatrolPlanDetailVo vo = new PatrolPlanDetailVo();
        BeanUtils.copyProperties(patrolPlan, vo);

        // 查询关联摄像头
        List<PlanCamera> cameras = planCameraService.selectByPlanId(id);
        vo.setCameras(cameras);

        return vo;
    }
}
