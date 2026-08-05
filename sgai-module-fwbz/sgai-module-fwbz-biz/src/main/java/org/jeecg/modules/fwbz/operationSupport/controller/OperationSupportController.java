package org.jeecg.modules.fwbz.operationSupport.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.dto.DeviceDataFindDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.AirConditioningUnitStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.FreshAirStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.MeteringPointDataDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.PowerStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.vo.MeteringPointDataChartVo;
import org.jeecg.modules.fwbz.mdm.entity.Device;
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
     * 新风机组-列表
     * @param params
     * @return
     */
    @GetMapping("/freshAirHandlingUnitList")
    public Result<IPage<DeviceDataVo>> freshAirHandlingUnitList(DeviceDataFindDto params) {
        return Result.ok(service.freshAirHandlingUnitList(params));
    }

        /**
     * 配电系统-列表
     * @param params
     * @return
     */
    @GetMapping("/powerDistributionSystemList")
    public Result<IPage<DeviceDataVo>> powerDistributionSystemList(DeviceDataFindDto params) {
        return Result.ok(service.powerDistributionSystemList(params));
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
     * 新风机组-数据统计
     * @param
     * @return
     */
    @GetMapping("/freshAirStatistics")
    public Result<FreshAirStatisticsDto> freshAirStatistics() {
        return Result.ok(service.freshAirStatistics());
    }
    /**
     * 配电系统-数据统计
     * @param
     * @return
     */
    @GetMapping("/powerStatistics")
    public Result<PowerStatisticsDto> powerStatistics() {
        return Result.ok(service.powerStatistics());
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

    /**
     * 新风机组- pm25
     * @param
     * @return
     */
    @GetMapping("/pm25")
    public Result<MeteringPointDataChartVo> pm25(MeteringPointDataDto param) {
        return Result.ok(new MeteringPointDataChartVo(service.pm25(param.getEnergyFlowDiagramIds(), param.getDay())));
    }

    /**
     * 新风机组-送风温度
     * @param
     * @return
     */
    @GetMapping("/freshSupplyAirTemperature")
    public Result<MeteringPointDataChartVo> freshSupplyAirTemperature(MeteringPointDataDto param) {
        return Result.ok(new MeteringPointDataChartVo(service.freshSupplyAirTemperature(param.getEnergyFlowDiagramIds(), param.getDay())));
    }


    /**
     * 新风机组-回风温度
     * @param
     * @return
     */
    @GetMapping("/freshReturnAirTemperature")
    public Result<MeteringPointDataChartVo> freshReturnAirTemperature(MeteringPointDataDto param) {
        return Result.ok(new MeteringPointDataChartVo(service.freshReturnAirTemperature(param.getEnergyFlowDiagramIds(), param.getDay())));
    }


    /**
     * 配电系统- 有功功率
     * @param
     * @return
     */
    @GetMapping("/activePower")
    public Result<MeteringPointDataChartVo> activePower(MeteringPointDataDto param) {
        return Result.ok(new MeteringPointDataChartVo(service.activePower(param.getEnergyFlowDiagramIds(), param.getDay())));
    }


}
