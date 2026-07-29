package org.jeecg.module.gather.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.module.gather.entity.EnergyDataGatherTime;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface IEnergyDataGatherTimeService extends IService<EnergyDataGatherTime> {

    void saveGatherData(String deviceCode, LocalDateTime time, BigDecimal value);

    List<EnergyDataGatherTime> findAll();
}
