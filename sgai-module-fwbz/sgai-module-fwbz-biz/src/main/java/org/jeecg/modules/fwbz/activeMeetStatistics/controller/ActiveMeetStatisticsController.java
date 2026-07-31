package org.jeecg.modules.fwbz.activeMeetStatistics.controller;

import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.activeMeetStatistics.service.IActiveMeetStatisticsService;
import org.jeecg.modules.fwbz.activeMeetStatistics.vo.StatCardVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 排期统计卡片
 */
@RestController
@RequestMapping("/fwbz/activeMeetStatistics")
@AllArgsConstructor
public class ActiveMeetStatisticsController {

    private final IActiveMeetStatisticsService service;

    /**
     * 本月活动数（含较上月对比）
     *
     * @return
     */
    @GetMapping("/countThisMonth")
    //@RequiresPermissions("fwbz:activeMeetStatistics:countThisMonth")
    @AutoLog(value = "活动统计-本月活动数")
    public Result<StatCardVO> countThisMonth() {
        return Result.ok(service.countThisMonth());
    }

    /**
     * 今日活动数
     *
     * @return
     */
    @GetMapping("/countToday")
    //@RequiresPermissions("fwbz:activeMeetStatistics:countToday")
    @AutoLog(value = "活动统计-今日活动数")
    public Result<StatCardVO> countToday() {
        return Result.ok(service.countToday());
    }

    /**
     * 下周活动数（待筹备）
     *
     * @return
     */
    @GetMapping("/countNextWeek")
    //@RequiresPermissions("fwbz:activeMeetStatistics:countNextWeek")
    @AutoLog(value = "活动统计-下周活动数")
    public Result<StatCardVO> countNextWeek() {
        return Result.ok(service.countNextWeek());
    }

    /**
     * 场馆利用率（含较上月对比）
     *
     * @return
     */
    @GetMapping("/venueUtilization")
    //@RequiresPermissions("fwbz:activeMeetStatistics:venueUtilization")
    @AutoLog(value = "活动统计-场馆利用率")
    public Result<StatCardVO> venueUtilization() {
        return Result.ok(service.venueUtilization());
    }

    /**
     * 汇总统计（返回全部卡片）
     *
     * @return
     */
    @GetMapping("/summary")
    //@RequiresPermissions("fwbz:activeMeetStatistics:summary")
    @AutoLog(value = "活动统计-汇总")
    public Result<List<StatCardVO>> summary() {
        return Result.ok(service.getSummary());
    }
}
