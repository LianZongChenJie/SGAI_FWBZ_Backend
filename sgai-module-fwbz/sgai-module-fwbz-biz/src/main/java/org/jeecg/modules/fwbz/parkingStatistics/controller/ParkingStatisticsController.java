package org.jeecg.modules.fwbz.parkingStatistics.controller;

import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.parkingStatistics.service.IParkingStatisticsService;
import org.jeecg.modules.fwbz.parkingStatistics.vo.ParkingFlow24hVO;
import org.jeecg.modules.fwbz.parkingStatistics.vo.ParkingSpaceStatVO;
import org.jeecg.modules.fwbz.parkingStatistics.vo.ParkingStatCardVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 停车统计
 * <p>
 * 数据由定时任务 ParkingStatisticsJob 每5分钟同步到数据库，接口直接读库返回。
 * 停车场实时车位分布和24小时流量趋势从外部系统实时获取，不落库。
 */
@RestController
@RequestMapping("/fwbz/parkingStatistics")
@AllArgsConstructor
public class ParkingStatisticsController {

    private final IParkingStatisticsService service;

    /**
     * 今日进场车辆（读库，数据由定时任务每5分钟同步）
     */
    @GetMapping("/todayEntryCount")
    @AutoLog(value = "停车统计-今日进场车辆")
    public Result<ParkingStatCardVO> todayEntryCount() {
        return Result.ok(service.queryTodayEntryCount());
    }

    /**
     * 当前在场车辆（读库，数据由定时任务每5分钟同步）
     */
    @GetMapping("/currentInCount")
    @AutoLog(value = "停车统计-当前在场车辆")
    public Result<ParkingStatCardVO> currentInCount() {
        return Result.ok(service.queryCurrentInCount());
    }

    /**
     * 剩余车位（读库，数据由定时任务每5分钟同步）
     */
    @GetMapping("/remainingSpaceCount")
    @AutoLog(value = "停车统计-剩余车位")
    public Result<ParkingStatCardVO> remainingSpaceCount() {
        return Result.ok(service.queryRemainingSpaceCount());
    }

    /**
     * 平均停车时长（读库，数据由定时任务每5分钟同步）
     */
    @GetMapping("/averageParkingDuration")
    @AutoLog(value = "停车统计-平均停车时长")
    public Result<ParkingStatCardVO> averageParkingDuration() {
        return Result.ok(service.queryAverageParkingDuration());
    }

    /**
     * 汇总（读库，数据由定时任务每5分钟同步）
     */
    @GetMapping("/summary")
    @AutoLog(value = "停车统计-汇总")
    public Result<List<ParkingStatCardVO>> summary() {
        return Result.ok(service.querySummary());
    }

    /**
     * 停车场实时车位分布（"停车场实时状态"统计图数据）
     * <p>
     * 直接从外部系统获取，不落库，示例：P1: 156/300 | P2: 89/200 | P3: 211/400 | P4: 0/300
     */
    @GetMapping("/parkingSpaceDistribution")
    @AutoLog(value = "停车统计-停车场实时车位分布")
    public Result<List<ParkingSpaceStatVO>> parkingSpaceDistribution() {
        return Result.ok(service.getParkingSpaceDistribution());
    }

    /**
     * 24 小时停车流量趋势（"停车流量趋势"统计图数据）
     * <p>
     * 直接从外部系统获取，不落库，保持外部API原始返回格式
     */
    @GetMapping("/parkingFlow24h")
    @AutoLog(value = "停车统计-24小时停车流量")
    public Result<ParkingFlow24hVO> parkingFlow24h() {
        return Result.ok(service.getParkingFlow24h());
    }
}
