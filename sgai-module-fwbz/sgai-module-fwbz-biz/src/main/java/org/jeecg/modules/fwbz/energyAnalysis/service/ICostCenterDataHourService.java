package org.jeecg.modules.fwbz.energyAnalysis.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.energyAnalysis.entity.CostCenterDataHour;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ICostCenterDataHourService extends IService<CostCenterDataHour> {

    void save(String type, Long relId, LocalDateTime time, BigDecimal value, BigDecimal cost);
}
