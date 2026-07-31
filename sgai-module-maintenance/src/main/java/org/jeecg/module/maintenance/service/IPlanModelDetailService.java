package org.jeecg.module.maintenance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.module.maintenance.entity.PlanModelDetail;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface IPlanModelDetailService extends IService<PlanModelDetail> {

    void removeByPlanModelIds(Collection<Long> planModelIds);

    List<PlanModelDetail> queryByStartRangeAndEnableFlag(LocalDate start, LocalDate end, boolean enableFlag);

    void updateEnableFlag(Long detailId, boolean enableFlag);
}
