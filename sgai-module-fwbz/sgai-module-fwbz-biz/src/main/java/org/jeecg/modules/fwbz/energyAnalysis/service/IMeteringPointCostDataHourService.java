package org.jeecg.modules.fwbz.energyAnalysis.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.energyAnalysis.entity.MeteringPointCostDataHour;

import java.time.LocalDateTime;
import java.util.List;

public interface IMeteringPointCostDataHourService extends IService<MeteringPointCostDataHour> {

    List<MeteringPointCostDataHour> findByTimeRangeAndPointIds(LocalDateTime startTime, LocalDateTime endTime, List<Long> pointIds);

    MeteringPointCostDataHour findByTimeAndPointId(LocalDateTime time, Long pointId);
}
