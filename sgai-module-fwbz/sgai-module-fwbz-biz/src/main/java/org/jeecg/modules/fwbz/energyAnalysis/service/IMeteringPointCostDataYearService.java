package org.jeecg.modules.fwbz.energyAnalysis.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.energyAnalysis.entity.MeteringPointCostDataYear;

import java.time.LocalDateTime;
import java.util.List;

public interface IMeteringPointCostDataYearService extends IService<MeteringPointCostDataYear> {

    MeteringPointCostDataYear findByTimeAndPointId(LocalDateTime time, Long pointId);

    List<MeteringPointCostDataYear> findByTimeRangeAndPointIds(LocalDateTime startDate, LocalDateTime endDate, List<Long> pointIds);
}
