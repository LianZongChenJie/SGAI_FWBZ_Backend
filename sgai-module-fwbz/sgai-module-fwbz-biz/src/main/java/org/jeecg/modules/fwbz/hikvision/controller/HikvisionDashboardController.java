package org.jeecg.modules.fwbz.hikvision.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.hikvision.dto.*;
import org.jeecg.modules.fwbz.hikvision.entity.EventNotify;
import org.jeecg.modules.fwbz.hikvision.entity.PersonRecognition;
import org.jeecg.modules.fwbz.hikvision.service.IHikvisionDashboardTaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 海康数据看板接口
 * <p>全部统计数据从数据库读取，由定时任务 HikvisionDashboardJob 每5分钟同步。</p>
 *
 * @author fwbz
 */
@Api(tags = "海康数据看板")
@RestController
@RequestMapping("/fwbz/hikvision/dashboard")
@AllArgsConstructor
public class HikvisionDashboardController {

    private final IHikvisionDashboardTaskService dashboardTaskService;

    /**
     * 获取今日进场人数（从 table_personnel_statistics 读取）
     */
    @GetMapping("/todayEntryCount")
    @ApiOperation(value = "获取今日进场人数", notes = "从 table_personnel_statistics 查询，由定时任务每5分钟同步")
    public Result<TodayEntryCountVO> getTodayEntryCount() {
        return Result.OK(dashboardTaskService.queryTodayEntryCount());
    }

    /**
     * 获取当前在场人数（从 table_personnel_statistics 读取）
     */
    @GetMapping("/currentOnsiteCount")
    @ApiOperation(value = "获取当前在场人数", notes = "从 table_personnel_statistics 查询，由定时任务每5分钟同步")
    public Result<CurrentOnsiteCountVO> getCurrentOnsiteCount() {
        return Result.OK(dashboardTaskService.queryCurrentOnsiteCount());
    }

    /**
     * 查询人员识别记录（从 table_person_recognition 分页读取）
     */
    @PostMapping("/recognitionRecords")
    @ApiOperation(value = "查询人员识别记录", notes = "从 table_person_recognition 分页查询今日记录")
    public Result<Page<PersonRecognition>> getRecognitionRecords(
            @RequestBody @ApiParam(value = "分页参数") RecognitionRecordRequest request) {
        int pageNo = request.getPageNo() != null ? request.getPageNo() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 10;
        return Result.OK(dashboardTaskService.queryRecognitionRecords(pageNo, pageSize));
    }

    /**
     * 查询异常行为预警（从 table_event_notify 分页读取）
     */
    @PostMapping("/abnormalBehaviorAlerts")
    @ApiOperation(value = "查询异常行为预警", notes = "从 table_event_notify 分页查询今日记录")
    public Result<Page<EventNotify>> getAbnormalBehaviorAlerts(
            @RequestBody @ApiParam(value = "分页参数") AbnormalBehaviorAlertRequest request) {
        int pageNo = request.getPageNo() != null ? request.getPageNo() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 10;
        return Result.OK(dashboardTaskService.queryAbnormalAlerts(pageNo, pageSize));
    }

    // ==================== 看板统计卡片（从数据库读取，含较昨日趋势） ====================

    @GetMapping("/stat/todayEntryCount")
    @ApiOperation(value = "今日进场人数统计卡片", notes = "从 table_visitor_flow 读取今日进场人数及较昨日趋势")
    public Result<StatCardVO> todayEntryCountCard() {
        return Result.OK(dashboardTaskService.getTodayEntryCard());
    }

    @GetMapping("/stat/currentOnsiteCount")
    @ApiOperation(value = "当前在场人数统计卡片", notes = "从 table_visitor_flow 读取当前在场人数及较昨日趋势")
    public Result<StatCardVO> currentOnsiteCountCard() {
        return Result.OK(dashboardTaskService.getCurrentOnsiteCard());
    }

    @GetMapping("/stat/recognitionRecordCount")
    @ApiOperation(value = "人员识别记录统计卡片", notes = "从 table_person_recognition 读取今日人员识别记录数及较昨日趋势")
    public Result<StatCardVO> recognitionRecordCountCard() {
        return Result.OK(dashboardTaskService.getRecognitionRecordCard());
    }

    @GetMapping("/stat/abnormalAlertCount")
    @ApiOperation(value = "异常行为预警统计卡片", notes = "从 table_event_notify 读取今日异常行为预警数及较昨日趋势")
    public Result<StatCardVO> abnormalAlertCountCard() {
        return Result.OK(dashboardTaskService.getAbnormalAlertCard());
    }

    @GetMapping("/stat/summary")
    @ApiOperation(value = "看板统计卡片汇总", notes = "汇总返回四个看板统计卡片数据")
    public Result<List<StatCardVO>> summaryCards() {
        return Result.OK(dashboardTaskService.getSummaryCards());
    }
}
