package org.jeecg.module.maintenance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.module.maintenance.dto.PlanDto;
import org.jeecg.module.maintenance.dto.PlanParam;
import org.jeecg.module.maintenance.entity.Plan;
import org.jeecg.module.maintenance.entity.PlanModelDetail;

import java.time.LocalDateTime;
import java.util.List;

public interface IPlanService extends IService<Plan> {

    List<Plan> queryByBeginTimeRangeAndLabelType(LocalDateTime startTime, LocalDateTime endTime, String labelType);

    void createPlan(PlanModelDetail detail);

    void createPlan(List<PlanModelDetail> details);

    PlanDto findDetail(Long planId, Integer page, Integer pagesize, String name);

    void excuteNow(Long planId);

    void transferData(PlanParam param);
}
