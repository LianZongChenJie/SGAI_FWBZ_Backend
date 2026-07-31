package org.jeecg.modules.fwbz.energyAnalysis.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.energyAnalysis.entity.MeteringPointDataHour;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface IMeteringPointDataHourService extends IService<MeteringPointDataHour> {

    List<MeteringPointDataHour> findByTimeRangeAndPointIds(LocalDateTime startTime, LocalDateTime endTime, List<Long> pointIds);

    void save(Long pointId, LocalDateTime time, BigDecimal value);

    List<MeteringPointDataHour> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    List<MeteringPointDataHour> findByPointIdAndTimeRange(Long pointId, LocalDateTime startTime, LocalDateTime endTime);

    MeteringPointDataHour findByPointIdAndTime(Long pointId,LocalDateTime hour);
}
