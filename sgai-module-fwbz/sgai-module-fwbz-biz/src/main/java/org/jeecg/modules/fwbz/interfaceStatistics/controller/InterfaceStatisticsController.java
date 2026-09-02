package org.jeecg.modules.fwbz.interfaceStatistics.controller;

import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.activeMeetStatistics.vo.StatCardVO;
import org.jeecg.modules.fwbz.interfaceStatistics.service.IInterfaceStatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 接口统计卡片
 */
@RestController
@RequestMapping("/fwbz/interfaceStatistics")
@AllArgsConstructor
public class InterfaceStatisticsController {

    private final IInterfaceStatisticsService service;

    /**
     * 对接系统数
     *
     * @return
     */
    @GetMapping("/connectedSystemCount")
    //@RequiresPermissions("fwbz:interfaceStatistics:connectedSystemCount")
    @AutoLog(value = "接口统计-对接系统数")
    public Result<StatCardVO> connectedSystemCount() {
        return Result.ok(service.connectedSystemCount());
    }

    /**
     * 接口在线率
     *
     * @return
     */
    @GetMapping("/onlineRate")
    //@RequiresPermissions("fwbz:interfaceStatistics:onlineRate")
    @AutoLog(value = "接口统计-接口在线率")
    public Result<StatCardVO> onlineRate() {
        return Result.ok(service.onlineRate());
    }

    /**
     * 今日数据量（含较昨日对比）
     *
     * @return
     */
    @GetMapping("/todayDataSize")
    //@RequiresPermissions("fwbz:interfaceStatistics:todayDataSize")
    @AutoLog(value = "接口统计-今日数据量")
    public Result<StatCardVO> todayDataSize() {
        return Result.ok(service.todayDataSize());
    }

    /**
     * 异常接口数
     *
     * @return
     */
    @GetMapping("/abnormalCount")
    //@RequiresPermissions("fwbz:interfaceStatistics:abnormalCount")
    @AutoLog(value = "接口统计-异常接口")
    public Result<StatCardVO> abnormalCount() {
        return Result.ok(service.abnormalCount());
    }

    /**
     * 汇总统计（返回全部卡片）
     *
     * @return
     */
    @GetMapping("/summary")
    //@RequiresPermissions("fwbz:interfaceStatistics:summary")
    @AutoLog(value = "接口统计-汇总")
    public Result<List<StatCardVO>> summary() {
        return Result.ok(service.getSummary());
    }
}
