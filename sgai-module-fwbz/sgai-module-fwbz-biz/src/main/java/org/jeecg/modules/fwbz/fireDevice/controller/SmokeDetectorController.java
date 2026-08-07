package org.jeecg.modules.fwbz.fireDevice.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.activeMeetStatistics.vo.StatCardVO;
import org.jeecg.modules.fwbz.fireDevice.entity.FireAlarmRecord;
import org.jeecg.modules.fwbz.fireDevice.entity.SmokeDetector;
import org.jeecg.modules.fwbz.fireDevice.service.ISmokeDetectorService;
import org.jeecg.modules.fwbz.fireDevice.vo.StatusCountVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * 消防设备 控制器
 *
 * @author fwbz
 */
@Slf4j
@Api(tags = "消防设备")
@RestController
@RequestMapping("/fwbz/fireDevice/smokeDetector")
@AllArgsConstructor
public class SmokeDetectorController {

    private final ISmokeDetectorService smokeDetectorService;

    /**
     * 分页查询消防设备列表，联动返回设备类型名称（typeName）。
     *
     * @param pageNo     当前页码，默认 1
     * @param pageSize   每页条数，默认 10
     * @param deviceName 设备名称（模糊查询）
     * @param status     状态
     * @param deviceType 设备类型ID
     * @param venueId    场馆ID
     * @param startTime  最后巡检时间-开始
     * @param endTime    最后巡检时间-结束
     * @param signal     信号强度
     * @param powerLevel 电量
     * @return 分页结果
     */
    @ApiOperation("分页查询消防设备列表")
    @GetMapping("/list")
    public Result<IPage<SmokeDetector>> list(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String deviceType,
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
            @RequestParam(required = false) String signal,
            @RequestParam(required = false) String powerLevel) {

        IPage<SmokeDetector> page = new Page<>(pageNo, pageSize);
        IPage<SmokeDetector> result = smokeDetectorService.getSmokeDetectorPage(
                page, deviceName, status, deviceType, venueId,
                startTime, endTime, signal, powerLevel);

        return Result.OK(result);
    }

    /**
     * 根据消防设备ID分页查询报警记录。
     *
     * @param deviceId 消防设备ID（必填）
     * @param pageNo   当前页码，默认 1
     * @param pageSize 每页条数，默认 10
     * @return 报警记录分页结果
     */
    @ApiOperation("根据设备ID查询报警记录")
    @GetMapping("/alarmRecords")
    public Result<IPage<FireAlarmRecord>> alarmRecords(
            @RequestParam Long deviceId,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        IPage<FireAlarmRecord> page = new Page<>(pageNo, pageSize);
        IPage<FireAlarmRecord> result = smokeDetectorService.getAlarmRecordsByDeviceId(page, deviceId);

        return Result.OK(result);
    }

    @AutoLog(value = "统计-消防设备总数")
    @ApiOperation("统计消防设备总数")
    @GetMapping("/count/total")
    public Result<StatCardVO> countTotal() {
        log.info("查询消防设备总数");
        return Result.ok(smokeDetectorService.countTotal());
    }

    @AutoLog(value = "统计-设备在线率")
    @ApiOperation("统计设备在线率")
    @GetMapping("/count/online")
    public Result<StatCardVO> countOnline() {
        log.info("查询设备在线率");
        return Result.ok(smokeDetectorService.countOnline());
    }

    @AutoLog(value = "统计-今日巡检完成")
    @ApiOperation("统计今日巡检完成数量")
    @GetMapping("/count/todayCheck")
    public Result<StatCardVO> countTodayCheck() {
        log.info("查询今日巡检完成数量");
        return Result.ok(smokeDetectorService.countTodayCheck());
    }

    @AutoLog(value = "统计-待处理告警")
    @ApiOperation("统计待处理告警数量")
    @GetMapping("/count/pendingAlarm")
    public Result<StatCardVO> countPendingAlarm() {
        log.info("查询待处理告警数量");
        return Result.ok(smokeDetectorService.countPendingAlarm());
    }

    @AutoLog(value = "统计-消防设备汇总")
    @ApiOperation("消防设备统计汇总")
    @GetMapping("/summary")
    public Result<List<StatCardVO>> summary() {
        log.info("查询消防设备统计汇总");
        return Result.ok(smokeDetectorService.getSummary());
    }

    @AutoLog(value = "统计-按设备状态统计")
    @ApiOperation("按设备状态统计数量")
    @GetMapping("/countByStatus")
    public Result<List<StatusCountVO>> countByStatus() {
        log.info("查询设备状态统计");
        return Result.ok(smokeDetectorService.countByStatus());
    }
}
