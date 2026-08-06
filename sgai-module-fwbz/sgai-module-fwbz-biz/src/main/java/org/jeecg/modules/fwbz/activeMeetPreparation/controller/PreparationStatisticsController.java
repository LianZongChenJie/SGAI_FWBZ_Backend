package org.jeecg.modules.fwbz.activeMeetPreparation.controller;

import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.activeMeetPreparation.service.IPreparationStatisticsService;
import org.jeecg.modules.fwbz.activeMeetStatistics.vo.StatCardVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 会前筹备统计卡片
 */
@RestController
@RequestMapping("/fwbz/preparationStatistics")
@AllArgsConstructor
public class PreparationStatisticsController {

    private final IPreparationStatisticsService service;

    /**
     * 待筹备会展（明天以后的活动数）
     * @return
     */
    @GetMapping("/pendingCount")
    @AutoLog(value = "会前筹备统计-待筹备会展")
    public Result<StatCardVO> pendingCount() {
        return Result.ok(service.pendingCount());
    }

    /**
     * 筹备完成率（待筹备会展的平均进度）
     * @return
     */
    @GetMapping("/completionRate")
    @AutoLog(value = "会前筹备统计-筹备完成率")
    public Result<StatCardVO> completionRate() {
        return Result.ok(service.completionRate());
    }

    /**
     * 明日开展（明天开始的活动数）
     * @return
     */
    @GetMapping("/tomorrowCount")
    @AutoLog(value = "会前筹备统计-明日开展")
    public Result<StatCardVO> tomorrowCount() {
        return Result.ok(service.tomorrowCount());
    }

    /**
     * 会展检查项（设备类型总量）
     * @return
     */
    @GetMapping("/checkItemCount")
    @AutoLog(value = "会前筹备统计-会展检查项")
    public Result<StatCardVO> checkItemCount() {
        return Result.ok(service.checkItemCount());
    }

    /**
     * 汇总统计（返回全部卡片）
     * @return
     */
    @GetMapping("/summary")
    @AutoLog(value = "会前筹备统计-汇总")
    public Result<List<StatCardVO>> summary() {
        return Result.ok(service.getSummary());
    }
}
