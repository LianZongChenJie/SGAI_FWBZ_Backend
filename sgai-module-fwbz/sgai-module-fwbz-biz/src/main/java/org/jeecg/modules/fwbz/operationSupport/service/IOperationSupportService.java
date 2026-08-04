package org.jeecg.modules.fwbz.operationSupport.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.modules.fwbz.dto.DeviceDataFindDto;
import org.jeecg.modules.fwbz.vo.DeviceDataVo;

public interface IOperationSupportService {

    IPage<DeviceDataVo> equipmentList(DeviceDataFindDto params) ;

    IPage<DeviceDataVo> airConditioningUnitList(DeviceDataFindDto params) ;


//    EnergyMeteringStatisticsDto statistics();

}
