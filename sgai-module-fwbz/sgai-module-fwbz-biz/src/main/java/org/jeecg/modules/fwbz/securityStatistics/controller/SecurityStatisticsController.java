package org.jeecg.modules.fwbz.securityStatistics.controller;

import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.securityStatistics.service.ISecurityStatisticsService;
import org.jeecg.modules.fwbz.securityStatistics.vo.SecurityStatCardVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 安防统计
 */
@RestController
@RequestMapping("/fwbz/securityStatistics")
@AllArgsConstructor
public class SecurityStatisticsController {

    private final ISecurityStatisticsService service;

    /**
     * 监控摄像头总数（含今日新增）
     */
    @GetMapping("/cameraTotal")
    @AutoLog(value = "安防统计-监控摄像头总数")
    public Result<SecurityStatCardVO> cameraTotal() {
        return Result.ok(service.cameraTotal());
    }

    /**
     * 在线摄像头数量（含在线率）
     */
    @GetMapping("/cameraOnline")
    @AutoLog(value = "安防统计-在线摄像头数量")
    public Result<SecurityStatCardVO> cameraOnline() {
        return Result.ok(service.cameraOnline());
    }

    /**
     * 今日视频巡更完成情况
     */
    @GetMapping("/patrolPlanToday")
    @AutoLog(value = "安防统计-今日视频巡更")
    public Result<SecurityStatCardVO> patrolPlanToday() {
        return Result.ok(service.patrolPlanToday());
    }

    /**
     * AI事件分析数量（较昨日对比）
     */
    @GetMapping("/aiEventAnalysis")
    @AutoLog(value = "安防统计-AI事件分析数量")
    public Result<SecurityStatCardVO> aiEventAnalysis() {
        return Result.ok(service.aiEventAnalysis());
    }

    /**
     * 汇总统计（返回全部卡片）
     */
    @GetMapping("/summary")
    @AutoLog(value = "安防统计-汇总")
    public Result<List<SecurityStatCardVO>> summary() {
        return Result.ok(service.getSummary());
    }
}
