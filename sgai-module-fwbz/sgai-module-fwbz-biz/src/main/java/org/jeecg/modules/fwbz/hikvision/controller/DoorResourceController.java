package org.jeecg.modules.fwbz.hikvision.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.hikvision.service.IDoorResourceService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 门禁点资源管理控制器
 * <p>触发从海康平台全量拉取门禁点数据并同步到本地数据库。
 * 同步策略：先清空表，再全量导入。</p>
 *
 * @author fwbz
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/fwbz/hikvision/door")
@Api(tags = "海康门禁点资源管理")
public class DoorResourceController {

    private final IDoorResourceService doorResourceService;

    /**
     * 触发全量同步海康门禁点数据
     * <p>请求无需参数，内部使用固定参数逐页拉取海康全部门禁点。
     * 先清空 table_door_resource 表，再批量插入新数据。</p>
     *
     * @return 同步结果（包含同步条数）
     */
    @PostMapping("/sync")
    @ApiOperation(value = "全量同步海康门禁点数据", notes = "先清空本地表，再从海康平台全量拉取门禁点数据导入")
    public Result<Integer> syncDoors() {
        try {
            int count = doorResourceService.syncFromHikvision();
            return Result.ok(count);
        } catch (Exception e) {
            log.error("同步门禁点数据失败", e);
            return Result.error("同步门禁点数据失败: " + e.getMessage());
        }
    }
}
