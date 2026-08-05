package org.jeecg.modules.fwbz.hikvision.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.hikvision.dto.StatCardVO;
import org.jeecg.modules.fwbz.hikvision.service.IDoorStatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 门禁统计控制器
 * <p>提供门禁点位和设备的总数、在线数统计，以及汇总查询。</p>
 *
 * @author fwbz
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/fwbz/hikvision/doorStatistics")
@Api(tags = "门禁统计")
public class DoorStatisticsController {

    private final IDoorStatisticsService doorStatisticsService;

    /**
     * 总门禁点位数量
     */
    @GetMapping("/countTotalDoorPoints")
    @ApiOperation(value = "总门禁点位数量", notes = "统计门禁点位总数及在线率")
    public Result<StatCardVO> countTotalDoorPoints() {
        return Result.ok(doorStatisticsService.countTotalDoorPoints());
    }

    /**
     * 在线门禁点位数量
     */
    @GetMapping("/countOnlineDoorPoints")
    @ApiOperation(value = "在线门禁点位数量", notes = "统计在线门禁点数及在线率")
    public Result<StatCardVO> countOnlineDoorPoints() {
        return Result.ok(doorStatisticsService.countOnlineDoorPoints());
    }

    /**
     * 门禁设备总数
     */
    @GetMapping("/countTotalDevices")
    @ApiOperation(value = "门禁设备总数", notes = "统计门禁设备总数及在线率")
    public Result<StatCardVO> countTotalDevices() {
        return Result.ok(doorStatisticsService.countTotalDevices());
    }

    /**
     * 在线门禁设备数
     */
    @GetMapping("/countOnlineDevices")
    @ApiOperation(value = "在线门禁设备数", notes = "统计在线门禁设备数及在线率")
    public Result<StatCardVO> countOnlineDevices() {
        return Result.ok(doorStatisticsService.countOnlineDevices());
    }

    /**
     * 汇总统计（返回全部卡片）
     */
    @GetMapping("/summary")
    @ApiOperation(value = "门禁统计汇总", notes = "一次性返回门禁点位总数、在线数、设备总数、在线数四个卡片")
    public Result<List<StatCardVO>> summary() {
        return Result.ok(doorStatisticsService.getSummary());
    }
}
