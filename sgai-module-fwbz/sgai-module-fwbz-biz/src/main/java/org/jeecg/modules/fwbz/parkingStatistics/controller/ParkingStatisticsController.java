package org.jeecg.modules.fwbz.parkingStatistics.controller;

import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.parkingStatistics.service.IParkingStatisticsService;
import org.jeecg.modules.fwbz.parkingStatistics.vo.ParkingStatCardVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 停车统计
 * <p>
 * 查询卡片时自动同步外部数据 → 写入DB → 返回前端
 */
@RestController
@RequestMapping("/fwbz/parkingStatistics")
@AllArgsConstructor
public class ParkingStatisticsController {

    private final IParkingStatisticsService service;

    /**
     * 今日进场车辆（同步外部 → 写入DB → 返回）
     */
    @GetMapping("/todayEntryCount")
    @AutoLog(value = "停车统计-今日进场车辆")
    public Result<ParkingStatCardVO> todayEntryCount() {
        return Result.ok(service.todayEntryCount());
    }

    /**
     * 当前在场车辆（同步外部 → 写入DB → 返回）
     */
    @GetMapping("/currentInCount")
    @AutoLog(value = "停车统计-当前在场车辆")
    public Result<ParkingStatCardVO> currentInCount() {
        return Result.ok(service.currentInCount());
    }

    /**
     * 剩余车位（同步外部 → 写入DB → 返回）
     */
    @GetMapping("/remainingSpaceCount")
    @AutoLog(value = "停车统计-剩余车位")
    public Result<ParkingStatCardVO> remainingSpaceCount() {
        return Result.ok(service.remainingSpaceCount());
    }

    /**
     * 平均停车时长（同步外部 → 写入DB → 返回）
     */
    @GetMapping("/averageParkingDuration")
    @AutoLog(value = "停车统计-平均停车时长")
    public Result<ParkingStatCardVO> averageParkingDuration() {
        return Result.ok(service.averageParkingDuration());
    }

    /**
     * 汇总（同步四项 → 写入DB → 返回4张卡片）
     */
    @GetMapping("/summary")
    @AutoLog(value = "停车统计-汇总")
    public Result<List<ParkingStatCardVO>> summary() {
        return Result.ok(service.getSummary());
    }
}
