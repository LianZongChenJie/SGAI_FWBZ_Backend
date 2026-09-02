package org.jeecg.modules.fwbz.activeMeetReport.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.activeMeetReport.service.IActiveMeetReportStatisticsService;
import org.jeecg.modules.fwbz.activeMeetReport.vo.StatCardVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 活动报告统计接口
 */
@Api(tags = "活动报告统计")
@Slf4j
@RestController
@RequestMapping("/fwbz/activeMeetReport/statistics")
@AllArgsConstructor
public class ActiveMeetReportStatisticsController {

    private final IActiveMeetReportStatisticsService activeMeetReportStatisticsService;

    /**
     * 待总结展会数
     */
    @GetMapping("/countPendingSummary")
    @AutoLog(value = "活动报告统计-待总结展会数")
    @ApiOperation(value = "待总结展会数", notes = "统计状态为待总结（0）的展会数量")
    public Result<StatCardVO> countPendingSummary() {
        return Result.ok(activeMeetReportStatisticsService.countPendingSummary());
    }

    /**
     * 已总结展会数（含较上月对比）
     */
    @GetMapping("/countSummarized")
    @AutoLog(value = "活动报告统计-已总结展会数")
    @ApiOperation(value = "已总结展会数", notes = "统计本月状态为已总结（1）的展会数量，并较上月对比")
    public Result<StatCardVO> countSummarized() {
        return Result.ok(activeMeetReportStatisticsService.countSummarized());
    }

    /**
     * 报告生成
     */
    @GetMapping("/reportGeneration")
    @AutoLog(value = "活动报告统计-报告生成")
    @ApiOperation(value = "报告生成", notes = "报告生成方式说明")
    public Result<StatCardVO> reportGeneration() {
        return Result.ok(activeMeetReportStatisticsService.reportGeneration());
    }

    /**
     * 知识库积累
     */
    @GetMapping("/knowledgeBaseAccumulation")
    @AutoLog(value = "活动报告统计-知识库积累")
    @ApiOperation(value = "知识库积累", notes = "知识库经验积累数量")
    public Result<StatCardVO> knowledgeBaseAccumulation() {
        return Result.ok(activeMeetReportStatisticsService.knowledgeBaseAccumulation());
    }

    /**
     * 汇总统计（返回全部卡片）
     */
    @GetMapping("/summary")
    @AutoLog(value = "活动报告统计-汇总")
    @ApiOperation(value = "汇总统计", notes = "返回活动报告全部统计卡片")
    public Result<List<StatCardVO>> summary() {
        return Result.ok(activeMeetReportStatisticsService.getSummary());
    }
}
