package org.jeecg.modules.fwbz.hikvision.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.hikvision.dto.*;
import org.jeecg.modules.fwbz.hikvision.service.IHikvisionDashboardService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 海康数据看板控制器
 * <p>提供今日进场人数、当前在场人数、人员识别记录、异常行为预警四个看板接口。</p>
 *
 * @author fwbz
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/fwbz/hikvision/dashboard")
@Api(tags = "海康数据看板")
public class HikvisionDashboardController {

    private final IHikvisionDashboardService dashboardService;

    /**
     * 获取今日进场人数
     * <p>查询海康ACS门禁系统中今日进场的总人数。</p>
     *
     * @return 今日进场人数
     */
    @PostMapping("/todayEntryCount")
    @ApiOperation(value = "获取今日进场人数", notes = "查询今日ACS门禁进场事件总数")
    public Result<TodayEntryCountVO> getTodayEntryCount() {
        try {
            TodayEntryCountVO result = dashboardService.getTodayEntryCount();
            return Result.ok(result);
        } catch (Exception e) {
            log.error("获取今日进场人数失败", e);
            return Result.error("获取今日进场人数失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前在场人数
     * <p>查询当前各区域/场所内的实时在场人数。</p>
     *
     * @return 当前在场人数
     */
    @PostMapping("/currentOnsiteCount")
    @ApiOperation(value = "获取当前在场人数", notes = "查询当前区域/场所实时在场人数")
    public Result<CurrentOnsiteCountVO> getCurrentOnsiteCount() {
        try {
            CurrentOnsiteCountVO result = dashboardService.getCurrentOnsiteCount();
            return Result.ok(result);
        } catch (Exception e) {
            log.error("获取当前在场人数失败", e);
            return Result.error("获取当前在场人数失败: " + e.getMessage());
        }
    }

    /**
     * 查询人员识别记录
     * <p>根据时间范围分页查询人脸识别事件记录。</p>
     *
     * @param request 查询参数（startTime, endTime, pageNo, pageSize）
     * @return 人员识别记录列表
     */
    @PostMapping("/recognitionRecords")
    @ApiOperation(value = "查询人员识别记录", notes = "根据时间范围分页查询人脸识别事件记录")
    public Result<RecognitionRecordResponse> getRecognitionRecords(@RequestBody RecognitionRecordRequest request) {
        try {
            RecognitionRecordResponse result = dashboardService.getRecognitionRecords(request);
            return Result.ok(result);
        } catch (Exception e) {
            log.error("查询人员识别记录失败", e);
            return Result.error("查询人员识别记录失败: " + e.getMessage());
        }
    }

    /**
     * 查询异常行为预警
     * <p>根据时间范围分页查询异常行为告警事件。</p>
     *
     * @param request 查询参数（startTime, endTime, pageNo, pageSize, eventTypes）
     * @return 异常行为预警列表
     */
    @PostMapping("/abnormalBehaviorAlerts")
    @ApiOperation(value = "查询异常行为预警", notes = "根据时间范围分页查询异常行为告警事件")
    public Result<AbnormalBehaviorAlertResponse> getAbnormalBehaviorAlerts(@RequestBody AbnormalBehaviorAlertRequest request) {
        try {
            AbnormalBehaviorAlertResponse result = dashboardService.getAbnormalBehaviorAlerts(request);
            return Result.ok(result);
        } catch (Exception e) {
            log.error("查询异常行为预警失败", e);
            return Result.error("查询异常行为预警失败: " + e.getMessage());
        }
    }
}
