package org.jeecg.modules.fwbz.centralizedAirCooling.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.centralizedAirCooling.dto.CentralizedAirCoolingOverviewDto;
import org.jeecg.modules.fwbz.centralizedAirCooling.service.CentralizedAirCoolingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 集中风冷系统 Controller
 */
@Slf4j
@RestController
@RequestMapping("/fwbz/centralizedAirCooling")
@RequiredArgsConstructor
@Api(tags = "集中风冷系统")
public class CentralizedAirCoolingController {

    private final CentralizedAirCoolingService centralizedAirCoolingService;

    @GetMapping("/overview")
    @ApiOperation(value = "总览数据", notes = "风冷系统总功率/当前制冷量/风冷系统COP/今日累计用电量")
    public Result<CentralizedAirCoolingOverviewDto> overview() {
        try {
            return Result.ok(centralizedAirCoolingService.getOverview());
        } catch (Exception e) {
            log.error("查询集中风冷总览数据异常", e);
            return Result.error("查询集中风冷总览数据异常: " + e.getMessage());
        }
    }
}
