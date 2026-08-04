package org.jeecg.modules.fwbz.venueVisitorFlow.controller;

import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.venueVisitorFlow.service.IVenueFlowService;
import org.jeecg.modules.fwbz.venueVisitorFlow.service.IVenueVisitorFlowService;
import org.jeecg.modules.fwbz.venueVisitorFlow.vo.VenueFlowVO;
import org.jeecg.modules.fwbz.venueVisitorFlow.vo.VisitorFlowCardVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 场馆客流统计 Controller
 * <p>
 * 提供两类接口：
 * <ul>
 *     <li>整体四张卡片（今日总客流/当前在场/峰值客流/平均停留）</li>
 *     <li>各场馆客流统计表格</li>
 * </ul>
 * 数据由定时任务每5分钟从海康同步到数据库，接口直接读库返回。
 * </p>
 *
 * @author fwbz
 */
@RestController
@RequestMapping("/fwbz/venueVisitorFlow")
@AllArgsConstructor
public class VenueVisitorFlowController {

    private final IVenueVisitorFlowService service;
    private final IVenueFlowService venueFlowService;

    /**
     * 今日总客流（读库，数据由定时任务每5分钟同步）
     */
    @GetMapping("/todayVisitorCount")
    @AutoLog(value = "场馆客流-今日总客流")
    public Result<VisitorFlowCardVO> todayVisitorCount() {
        return Result.ok(service.queryTodayVisitorCount());
    }

    /**
     * 当前在场（读库，数据由定时任务每5分钟同步）
     */
    @GetMapping("/currentVisitorCount")
    @AutoLog(value = "场馆客流-当前在场")
    public Result<VisitorFlowCardVO> currentVisitorCount() {
        return Result.ok(service.queryCurrentVisitorCount());
    }

    /**
     * 峰值客流（读库，数据由定时任务每5分钟同步）
     */
    @GetMapping("/peakVisitorCount")
    @AutoLog(value = "场馆客流-峰值客流")
    public Result<VisitorFlowCardVO> peakVisitorCount() {
        return Result.ok(service.queryPeakVisitorCount());
    }

    /**
     * 平均停留（读库，数据由定时任务每5分钟同步）
     */
    @GetMapping("/averageStopDuration")
    @AutoLog(value = "场馆客流-平均停留")
    public Result<VisitorFlowCardVO> averageStopDuration() {
        return Result.ok(service.queryAverageStopDuration());
    }

    /**
     * 整体四张卡片汇总（读库，数据由定时任务每5分钟同步）。
     */
    @GetMapping("/summary")
    @AutoLog(value = "场馆客流-汇总")
    public Result<List<VisitorFlowCardVO>> summary() {
        return Result.ok(service.querySummary());
    }

    /**
     * 各场馆客流统计表格（读库，数据由定时任务每5分钟同步）。
     * <p>对应前端"各场馆客流统计"表格：场馆 / 今日进场 / 当前在场 / 峰值人数 / 峰值时间 / 平均停留 / 较昨日 / 状态</p>
     */
    @GetMapping("/venueList")
    @AutoLog(value = "各场馆客流统计")
    public Result<List<VenueFlowVO>> venueList(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        if (date == null) {
            return Result.ok(venueFlowService.queryVenueFlowList());
        }
        return Result.ok(venueFlowService.queryVenueFlowListByDate(date));
    }

    /**
     * 手动触发各场馆客流同步（用于运维/调试）。
     */
    @GetMapping("/syncVenueFlow")
    @AutoLog(value = "各场馆客流-手动同步")
    public Result<Integer> syncVenueFlow() {
        return Result.ok(venueFlowService.syncAllVenueFlowFromHikvision());
    }
}