package org.jeecg.modules.fwbz.venueVisitorFlow.controller;

import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.venueVisitorFlow.service.IVenueVisitorFlowService;
import org.jeecg.modules.fwbz.venueVisitorFlow.vo.VisitorFlowCardVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 场馆客流统计 Controller
 * <p>
 * 调用海康四个 OpenAPI（今日总客流/当前在场/峰值客流/平均停留），
 * 同步入库并返回前端展示卡片。
 * </p>
 *
 * @author fwbz
 */
@RestController
@RequestMapping("/fwbz/venueVisitorFlow")
@AllArgsConstructor
public class VenueVisitorFlowController {

    private final IVenueVisitorFlowService service;

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
     * 汇总接口：一次性返回全部四张卡片（读库，数据由定时任务每5分钟同步）。
     */
    @GetMapping("/summary")
    @AutoLog(value = "场馆客流-汇总")
    public Result<List<VisitorFlowCardVO>> summary() {
        return Result.ok(service.querySummary());
    }
}
