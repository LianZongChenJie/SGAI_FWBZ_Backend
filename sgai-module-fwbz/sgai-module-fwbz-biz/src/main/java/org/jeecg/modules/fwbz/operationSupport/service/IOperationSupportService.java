package org.jeecg.modules.fwbz.operationSupport.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.modules.fwbz.dto.DeviceDataFindDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.AirConditioningUnitStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.FreshAirStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.OverViewStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.PowerStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.vo.Table;
import org.jeecg.modules.fwbz.mdm.dto.DeviceRunStateStatisticsDto;
import org.jeecg.modules.fwbz.vo.DeviceDataVo;

import java.time.LocalDate;
import java.util.List;

public interface IOperationSupportService {

    IPage<DeviceDataVo> deviceListWithAttrBycategoryId(DeviceDataFindDto params) ;

    IPage<DeviceDataVo> airConditioningUnitList(DeviceDataFindDto params) ;
    IPage<DeviceDataVo> airList(DeviceDataFindDto params) ;
    IPage<DeviceDataVo> freshAirHandlingUnitList(DeviceDataFindDto params) ;
    IPage<DeviceDataVo> powerDistributionSystemList(DeviceDataFindDto params) ;


    AirConditioningUnitStatisticsDto airConditioningUnitStatistics() ;
    FreshAirStatisticsDto freshAirStatistics() ;
    PowerStatisticsDto powerStatistics() ;
    OverViewStatisticsDto overviewStatistics() ;


    Table airEnergyFindDay(String energyFlowDiagramIds, LocalDate localDate);

    Table supplyAirTemperature(String energyFlowDiagramIds, LocalDate localDate);
    Table freshSupplyAirTemperature(String energyFlowDiagramIds, LocalDate localDate);
    Table returnAirTemperature(String energyFlowDiagramIds, LocalDate localDate);
    Table freshReturnAirTemperature(String energyFlowDiagramIds, LocalDate localDate);
    Table pm25(String energyFlowDiagramIds, LocalDate localDate);
    Table activePower(String energyFlowDiagramIds, LocalDate localDate);
    List<DeviceRunStateStatisticsDto> equipmentOverview(Long categoryId);
}
