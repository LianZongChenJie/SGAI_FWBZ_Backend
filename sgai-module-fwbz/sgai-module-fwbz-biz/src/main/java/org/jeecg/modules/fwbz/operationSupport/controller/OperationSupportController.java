package org.jeecg.modules.fwbz.operationSupport.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.main.dto.DeviceDataFindDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.*;
import org.jeecg.modules.fwbz.energyAnalysis.vo.MeteringPointDataChartVo;
import org.jeecg.modules.fwbz.mdm.entity.DeviceAttribute;
import org.jeecg.modules.fwbz.operationSupport.service.IOperationSupportService;
import org.jeecg.modules.fwbz.main.vo.DeviceDataVo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * 概览-空调机组列表
     * @param params
     * @return
     */
    @GetMapping("/overview/airList")
    public Result<IPage<DeviceDataVo>> airList(DeviceDataFindDto params) {
        return Result.ok(service.airList(params));
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
     * 排风机-数据统计
     * @param
     * @return
     */
    @GetMapping("/exhaustFanStatistics")
    public Result<ExhaustFanStatisticsDto> exhaustFanStatistics() {
        return Result.ok(service.exhaustFanStatistics());
    }
    /**
     * 风机盘管-数据统计
     * @param
     * @return
     */
    @GetMapping("/fanCoilStatistics")
    public Result<FanCoilStatisticsDto> fanCoilStatistics() {
        return Result.ok(service.fanCoilStatistics());
    }
    /**
     * 热回收-数据统计
     * @param
     * @return
     */
    @GetMapping("/heatRecoveryStatistics")
    public Result<HeatRecoveryStatisticsDto> heatRecoveryStatistics() {
        return Result.ok(service.heatRecoveryStatistics());
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


    /**
     * 概览-设备总览
     * @param categoryId 设备类别
     * @return 统计结果
     */
    @GetMapping("/equipmentOverview")
    public Result<?> equipmentOverview(@RequestParam(required = false) Long categoryId){
        return Result.ok(service.equipmentOverview(categoryId));
    }



    /**
     * 概览-数据统计
     * @param
     * @return
     */
    @GetMapping("/overviewStatistics")
    public Result<OverViewStatisticsDto> overviewStatistics() {
        return Result.ok(service.overviewStatistics());
    }

    /**
     * 空调控制
     *
     */
    @PostMapping("/airControl")
    public Result<String> airControl(@RequestBody List<DeviceAttribute> params){
        service.airControl(params);
        return Result.ok();
    }





}
