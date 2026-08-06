package org.jeecg.modules.fwbz.complaint.controller;

import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.activeMeetStatistics.vo.StatCardVO;
import org.jeecg.modules.fwbz.complaint.service.IComplaintStatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description: 首页统计卡片
 * @Author: jeecg-boot
 * @Date:   2026-08-06
 * @Version: V1.0
 */
@RestController
@RequestMapping("/fwbz/complaintStatistics")
@AllArgsConstructor
public class ComplaintStatisticsController {

    private final IComplaintStatisticsService service;

    /**
     * 今日会展
     */
    @GetMapping("/todayActiveMeet")
    @AutoLog(value = "首页统计-今日会展")
    public Result<StatCardVO> todayActiveMeet() {
        return Result.ok(service.todayActiveMeet());
    }

    /**
     * 调度指令
     */
    @GetMapping("/todayDispatchOrder")
    @AutoLog(value = "首页统计-调度指令")
    public Result<StatCardVO> todayDispatchOrder() {
        return Result.ok(service.todayDispatchOrder());
    }

    /**
     * 投诉建议
     */
    @GetMapping("/todayComplaint")
    @AutoLog(value = "首页统计-投诉建议")
    public Result<StatCardVO> todayComplaint() {
        return Result.ok(service.todayComplaint());
    }

    /**
     * 设备异常
     */
    @GetMapping("/todayAlarm")
    @AutoLog(value = "首页统计-设备异常")
    public Result<StatCardVO> todayAlarm() {
        return Result.ok(service.todayAlarm());
    }

    /**
     * 汇总统计
     */
    @GetMapping("/summary")
    @AutoLog(value = "首页统计-汇总")
    public Result<List<StatCardVO>> summary() {
        return Result.ok(service.getSummary());
    }
}
