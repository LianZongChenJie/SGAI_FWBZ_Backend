package org.jeecg.modules.fwbz.energyAnalysis.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.modules.fwbz.energyAnalysis.dto.EnergyMeteringStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.MeteringPointStatisticsDto;
import org.jeecg.modules.fwbz.mdm.entity.Device;

public interface IEenergyMeteringService {

    EnergyMeteringStatisticsDto statistics();

}
