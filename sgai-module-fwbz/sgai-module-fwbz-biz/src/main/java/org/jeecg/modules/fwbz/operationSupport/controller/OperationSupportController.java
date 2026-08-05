package org.jeecg.modules.fwbz.operationSupport.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.dto.DeviceDataFindDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.AirConditioningUnitStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.MeteringPointDataDto;
import org.jeecg.modules.fwbz.energyAnalysis.vo.MeteringPointDataChartVo;
import org.jeecg.modules.fwbz.operationSupport.service.IOperationSupportService;
import org.jeecg.modules.fwbz.vo.DeviceDataVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fwbz/operationSupport")
@AllArgsConstructor
@Api(tags="运行保障")
@Slf4j
public class OperationSupportController {

    private final IOperationSupportService service;


//    /**
//     * 状态统计
//     * @return 统计结果
//     */
//    @GetMapping("/statistics")
//    public Result<?> deviceRunStateStatistics(){
//        return Result.ok(service.statistics());
//    }



    @GetMapping("/equipmentList")
    public Result<IPage<DeviceDataVo>> equipmentList(DeviceDataFindDto params) {
        return Result.ok(service.equipmentList(params));
    }

    /**
     * 空调机组-列表
     * @param params
     * @return
     */
    @GetMapping("/airConditioningUnitList")
    public Result<IPage<DeviceDataVo>> airConditioningUnitList(DeviceDataFindDto params) {
        return Result.ok(service.airConditioningUnitList(params));
    }
    /**
     * 空调机组-数据统计
     * @param
     * @return
     */
    @GetMapping("/airConditioningUnitStatistics")
    public Result<AirConditioningUnitStatisticsDto> airConditioningUnitStatistics() {
        return Result.ok(service.airConditioningUnitStatistics());
    }


    /**
     * 空调机组-空调耗能趋势
     * @param
     * @return
     */
    @GetMapping("/airEnergyFindDay")
    public Result<MeteringPointDataChartVo> airEnergyFindDay(MeteringPointDataDto param) {
        return Result.ok(new MeteringPointDataChartVo(service.airEnergyFindDay(param.getEnergyFlowDiagramIds(), param.getDay())));
    }


    /**
     * 空调机组-送风温度
     * @param
     * @return
     */
    @GetMapping("/supplyAirTemperature")
    public Result<MeteringPointDataChartVo> supplyAirTemperature(MeteringPointDataDto param) {
        return Result.ok(new MeteringPointDataChartVo(service.supplyAirTemperature(param.getEnergyFlowDiagramIds(), param.getDay())));
    }


    /**
     * 空调机组-回风温度
     * @param
     * @return
     */
    @GetMapping("/returnAirTemperature")
    public Result<MeteringPointDataChartVo> returnAirTemperature(MeteringPointDataDto param) {
        return Result.ok(new MeteringPointDataChartVo(service.returnAirTemperature(param.getEnergyFlowDiagramIds(), param.getDay())));
    }



}
