package org.jeecg.modules.fwbz.runGuarantee.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.activeMeetStatistics.vo.StatCardVO;
import org.jeecg.modules.fwbz.runGuarantee.service.IRunGuaranteeService;
import org.jeecg.modules.fwbz.runGuarantee.vo.SystemDeviceStatVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/fwbz/runGuarantee")
@Api(tags = "运行保障")
public class RunGuaranteeController {

    private final IRunGuaranteeService runGuaranteeService;

    public RunGuaranteeController(IRunGuaranteeService runGuaranteeService) {
        this.runGuaranteeService = runGuaranteeService;
    }

    @GetMapping("/deviceStat")
    @ApiOperation("获取各系统设备在线统计")
    public Result<List<SystemDeviceStatVO>> getDeviceStat() {
        return Result.ok(runGuaranteeService.getDeviceStat());
    }

    @GetMapping("/linkTotal")
    @ApiOperation("链路总数（系统总数）")
    public Result<StatCardVO> getLinkTotal() {
        return Result.ok(runGuaranteeService.getLinkTotal());
    }

    @GetMapping("/normalLink")
    @ApiOperation("正常链路（消息总数）")
    public Result<StatCardVO> getNormalLink() {
        return Result.ok(runGuaranteeService.getNormalLink());
    }

    @GetMapping("/collectionStatus")
    @ApiOperation("数据采集状态（控制总数）")
    public Result<StatCardVO> getCollectionStatus() {
        return Result.ok(runGuaranteeService.getCollectionStatus());
    }

    @GetMapping("/processingStatus")
    @ApiOperation("数据处理状态（照明控制数）")
    public Result<StatCardVO> getProcessingStatus() {
        return Result.ok(runGuaranteeService.getProcessingStatus());
    }

    @GetMapping("/summary")
    @ApiOperation("运行保障汇总统计")
    public Result<List<StatCardVO>> getSummary() {
        return Result.ok(runGuaranteeService.getSummary());
    }
}
