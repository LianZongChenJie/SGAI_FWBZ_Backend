package org.jeecg.modules.fwbz.centralizedWaterCooling.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.centralizedWaterCooling.dto.CentralizedWaterCoolingOverviewDto;
import org.jeecg.modules.fwbz.centralizedWaterCooling.service.CentralizedWaterCoolingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 集中水冷系统 Controller
 */
@Slf4j
@RestController
@RequestMapping("/fwbz/centralizedWaterCooling")
@RequiredArgsConstructor
@Api(tags = "集中水冷系统")
public class CentralizedWaterCoolingController {

    private final CentralizedWaterCoolingService centralizedWaterCoolingService;

    @GetMapping("/overview")
    @ApiOperation(value = "总览数据", notes = "系统总功率/当前制冷量/系统效能COP/今日累计用电量")
    public Result<CentralizedWaterCoolingOverviewDto> overview() {
        try {
            return Result.ok(centralizedWaterCoolingService.getOverview());
        } catch (Exception e) {
            log.error("查询集中水冷总览数据异常", e);
            return Result.error("查询集中水冷总览数据异常: " + e.getMessage());
        }
    }
}
