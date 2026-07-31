package org.jeecg.modules.fwbz.energyAnalysis.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.energyAnalysis.entity.MeteringPointCostDataDay;

import java.time.LocalDateTime;
import java.util.List;

public interface IMeteringPointCostDataDayService extends IService<MeteringPointCostDataDay> {
    List<MeteringPointCostDataDay> findByTimeRangeAndPointIds(LocalDateTime startTime, LocalDateTime endTime, List<Long> pointIds);
    MeteringPointCostDataDay findByTimeAndPointId(LocalDateTime time, Long pointId);
}
