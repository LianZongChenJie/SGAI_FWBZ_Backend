package org.jeecg.module.maintenance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.module.maintenance.entity.PlanModelDetail;
import org.jeecg.module.maintenance.mapper.PlanModelDetailMapper;
import org.jeecg.module.maintenance.service.IPlanModelDetailService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Service
public class PlanModelDetailServiceImpl extends ServiceImpl<PlanModelDetailMapper, PlanModelDetail> implements IPlanModelDetailService {

    @Override
    public void removeByPlanModelIds(Collection<Long> planModelIds) {
        if(planModelIds == null || planModelIds.isEmpty()){
            return;
        }
        super.remove(new LambdaQueryWrapper<PlanModelDetail>().in(PlanModelDetail::getPlanModelId, planModelIds));
    }

    @Override
    public List<PlanModelDetail> queryByStartRangeAndEnableFlag(LocalDate start, LocalDate end, boolean enableFlag) {
        return super.list(new LambdaQueryWrapper<PlanModelDetail>()
                .eq(PlanModelDetail::getEnableFlag, enableFlag)
                .between( PlanModelDetail::getStart, start.atStartOfDay(), end.atStartOfDay()));
    }

    @Override
    public void updateEnableFlag(Long detailId, boolean enableFlag) {
        super.update(new LambdaUpdateWrapper<PlanModelDetail>().eq( PlanModelDetail::getId, detailId).set( PlanModelDetail::getEnableFlag, enableFlag));
    }
}
