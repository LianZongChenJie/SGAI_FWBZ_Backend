package org.jeecg.modules.fwbz.hikvision.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.hikvision.service.IDoorEventService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 门禁点事件管理控制器
 * <p>从海康平台增量拉取门禁点事件并同步到本地数据库。
 * 同步策略：以DB最新事件时间为起点，获取增量事件，按 event_id 去重后插入。</p>
 *
 * @author fwbz
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/fwbz/hikvision/doorEvent")
@Api(tags = "海康门禁点事件管理")
public class DoorEventController {

    private final IDoorEventService doorEventService;

    @PostMapping("/sync")
    @ApiOperation(value = "增量同步海康门禁点事件", notes = "以DB最新事件时间为startTime，当前时间为endTime，增量拉取事件并去重插入")
    public Result<Integer> syncEvents() {
        try {
            int count = doorEventService.syncFromHikvision();
            return Result.ok(count);
        } catch (Exception e) {
            log.error("同步门禁点事件失败", e);
            return Result.error("同步门禁点事件失败: " + e.getMessage());
        }
    }
}
