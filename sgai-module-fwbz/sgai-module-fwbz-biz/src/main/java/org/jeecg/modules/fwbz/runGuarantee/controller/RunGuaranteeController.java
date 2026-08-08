package org.jeecg.modules.fwbz.runGuarantee.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.api.vo.Result;
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
        List<SystemDeviceStatVO> statList = runGuaranteeService.getDeviceStat();
        return Result.ok(statList);
    }
}
