package org.jeecg.modules.fwbz.hikvision.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.hikvision.dto.DoorEventListVO;
import org.jeecg.modules.fwbz.hikvision.dto.DoorEventPageDto;
import org.jeecg.modules.fwbz.hikvision.service.IDoorEventService;
import org.springframework.web.bind.annotation.GetMapping;
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

    /**
     * 分页获取门禁点事件列表
     * <p>分页查询门禁点事件数据，支持按人员姓名、门禁点名称、门禁点编码、事件类型、进出类型、卡号、时间范围检索，为空查全部。</p>
     *
     * @param dto 分页及查询条件
     * @return 门禁点事件分页列表
     */
    @GetMapping("/list")
    @ApiOperation(value = "分页获取门禁点事件列表", notes = "分页查询门禁点事件数据，支持按人员姓名、门禁点名称、门禁点编码、事件类型、进出类型、卡号、时间范围检索，为空查全部")
    public Result<IPage<DoorEventListVO>> getEventList(DoorEventPageDto dto) {
        try {
            IPage<DoorEventListVO> page = doorEventService.getEventList(dto);
            return Result.ok(page);
        } catch (Exception e) {
            log.error("获取门禁点事件列表失败", e);
            return Result.error("获取门禁点事件列表失败: " + e.getMessage());
        }
    }
}
