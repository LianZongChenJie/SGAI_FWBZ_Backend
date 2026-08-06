package org.jeecg.modules.fwbz.fireDevice.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.fireDevice.entity.SmokeDetector;
import org.jeecg.modules.fwbz.fireDevice.service.ISmokeDetectorService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * 消防设备 控制器
 *
 * @author fwbz
 */
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
}
