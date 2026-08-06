package org.jeecg.modules.fwbz.hikvision.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.hikvision.entity.EventNotify;
import org.jeecg.modules.fwbz.hikvision.service.IEventNotifyService;
import org.springframework.web.bind.annotation.*;

/**
 * 事件通知查询接口
 *
 * @author fwbz
 */
@Api(tags = "事件通知")
@RestController
@RequestMapping("/fwbz/hikvision/eventNotify")
@AllArgsConstructor
public class EventNotifyController {

    private final IEventNotifyService eventNotifyService;

    /**
     * 分页查询事件通知记录
     * <p>支持按事件类型、状态、等级、事件源、时间范围等筛选，为空查全部</p>
     */
    @GetMapping("/list")
    @ApiOperation(value = "分页查询事件通知", notes = "支持多条件筛选，从 table_event_notify 分页查询")
    public Result<IPage<EventNotify>> getEventNotifyList(
            @ApiParam(value = "页码，从1开始", defaultValue = "1") @RequestParam(defaultValue = "1") int pageNo,
            @ApiParam(value = "每页条数", defaultValue = "10") @RequestParam(defaultValue = "10") int pageSize,
            @ApiParam(value = "事件类别（如：视频事件）") @RequestParam(required = false) String ability,
            @ApiParam(value = "事件类型，数值编码") @RequestParam(required = false) Integer eventType,
            @ApiParam(value = "事件状态：0-瞬时 1-开始 2-停止 4-联动结果更新 5-图片异步上传") @RequestParam(required = false) Integer status,
            @ApiParam(value = "事件等级：0-未配置 1-低 2-中 3-高") @RequestParam(required = false) Integer eventLvl,
            @ApiParam(value = "事件源编号，精确匹配") @RequestParam(required = false) String srcIndex,
            @ApiParam(value = "事件源名称，模糊匹配") @RequestParam(required = false) String srcName,
            @ApiParam(value = "事件源类型") @RequestParam(required = false) String srcType,
            @ApiParam(value = "事件发生开始时间") @RequestParam(required = false) String happenTimeStart,
            @ApiParam(value = "事件发生结束时间") @RequestParam(required = false) String happenTimeEnd) {
        return Result.OK(eventNotifyService.getEventNotifyList(
                pageNo, pageSize, ability, eventType, status, eventLvl,
                srcIndex, srcName, srcType, happenTimeStart, happenTimeEnd));
    }
}
