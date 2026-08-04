package org.jeecg.modules.fwbz.operationSupport.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.modules.fwbz.dto.DeviceDataFindDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.AirConditioningUnitStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.EnergyMeteringStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.vo.Table;
import org.jeecg.modules.fwbz.vo.DeviceDataVo;

import java.time.LocalDate;

public interface IOperationSupportService {

    IPage<DeviceDataVo> equipmentList(DeviceDataFindDto params) ;

    IPage<DeviceDataVo> airConditioningUnitList(DeviceDataFindDto params) ;


    AirConditioningUnitStatisticsDto airConditioningUnitStatistics() ;


    Table airEnergyFindDay(String energyFlowDiagramIds, LocalDate localDate);


//    EnergyMeteringStatisticsDto statistics();

}
