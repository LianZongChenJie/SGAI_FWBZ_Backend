package org.jeecg.modules.fwbz.branchAirCooling.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.branchAirCooling.dto.BranchAirCoolingOverviewDto;
import org.jeecg.modules.fwbz.branchAirCooling.service.BranchAirCoolingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分馆风冷系统 Controller
 */
@Slf4j
@RestController
@RequestMapping("/fwbz/branchAirCooling")
@RequiredArgsConstructor
@Api(tags = "分馆风冷系统")
public class BranchAirCoolingController {

    private final BranchAirCoolingService branchAirCoolingService;

    @GetMapping("/overview")
    @ApiOperation(value = "总览数据", notes = "分馆风冷总功率/当前制冷量/分馆综合COP/今日累计用电量")
    public Result<BranchAirCoolingOverviewDto> overview() {
        try {
            return Result.ok(branchAirCoolingService.getOverview());
        } catch (Exception e) {
            log.error("查询分馆风冷总览数据异常", e);
            return Result.error("查询分馆风冷总览数据异常: " + e.getMessage());
        }
    }
}
