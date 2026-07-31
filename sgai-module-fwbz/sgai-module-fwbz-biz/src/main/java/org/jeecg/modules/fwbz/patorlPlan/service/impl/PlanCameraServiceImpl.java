package org.jeecg.modules.fwbz.patorlPlan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.fwbz.patorlPlan.entity.PlanCamera;
import org.jeecg.modules.fwbz.patorlPlan.mapper.PlanCameraMapper;
import org.jeecg.modules.fwbz.patorlPlan.service.IPlanCameraService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 巡更计划关联摄像头
 * @Author: jeecg-boot
 * @Date:   2026-07-31
 * @Version: V1.0
 */
@Service
public class PlanCameraServiceImpl extends ServiceImpl<PlanCameraMapper, PlanCamera> implements IPlanCameraService {

    @Override
    public void deleteByPlanId(Long planId) {
        this.baseMapper.deleteByPlanId(planId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(Long planId, List<String> indexCodes) {
        List<PlanCamera> list = new ArrayList<>();
        for (String indexCode : indexCodes) {
            PlanCamera camera = new PlanCamera();
            camera.setPlanId(planId);
            camera.setIndexCode(indexCode);
            list.add(camera);
        }
        this.saveBatch(list);
    }

    @Override
    public List<PlanCamera> selectByPlanId(Long planId) {
        return this.baseMapper.selectByPlanId(planId);
    }
}
