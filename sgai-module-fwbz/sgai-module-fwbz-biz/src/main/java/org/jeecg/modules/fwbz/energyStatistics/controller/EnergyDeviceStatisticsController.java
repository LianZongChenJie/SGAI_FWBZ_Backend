package org.jeecg.modules.fwbz.energyStatistics.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.mdm.dto.DeviceRunStateStatisticsDto;
import org.jeecg.modules.fwbz.energyStatistics.service.IEnergyDeviceStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 能耗设备统计
 */
@Api(tags = "能耗设备统计")
@RestController
@RequestMapping("/fwbz/energyStatistics/device")
@Slf4j
public class EnergyDeviceStatisticsController {

    @Autowired
    private IEnergyDeviceStatisticsService energyDeviceStatisticsService;

    /**
     * 根据设备类别统计设备总数和在线数
     *
     * @param categoryId 设备类别id；为空统计全部
     * @return 统计结果（总数、在线数、离线数）
     */
    @ApiOperation(value = "按设备类别统计设备总数和在线数", notes = "前端传入设备类别id，统计该类别设备总数与在线数")
    @GetMapping("/statisticsByCategoryId")
    public Result<DeviceRunStateStatisticsDto> statisticsByCategoryId(@RequestParam(required = false) Long categoryId) {
        return Result.ok(energyDeviceStatisticsService.statisticsByCategoryId(categoryId));
    }
}
