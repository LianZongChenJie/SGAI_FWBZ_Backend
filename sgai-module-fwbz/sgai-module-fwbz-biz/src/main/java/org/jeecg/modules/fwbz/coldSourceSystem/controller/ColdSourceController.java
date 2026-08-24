package org.jeecg.modules.fwbz.coldSourceSystem.controller;

import com.sunwayland.pspace.entity.Base;
import com.sunwayland.pspace.entity.PsConnectInfo;
import com.sunwayland.pspace.entity.PsResult;
import com.sunwayland.pspace.entity.PsServerProp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.coldSourceSystem.dto.ColdSourceWriteDto;
import org.jeecg.modules.fwbz.coldSourceSystem.service.ColdSourceOverviewService;
import org.jeecg.modules.fwbz.coldSourceSystem.service.ColdSourceServerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 冷源系统(pSpace) —— 字段映射查询 + 服务器信息查询
 *
 * 实时数据统一由 WebSocket 订阅推送（前端连接 /fwbz/coldSource/ws 接收），
 * 不再提供 HTTP 轮询接口；本接口仅提供映射/服务器信息查询。
 */
@RestController
@RequestMapping("/fwbz/coldSource")
@AllArgsConstructor
@Slf4j
@Api(tags = "冷源系统")
public class ColdSourceController {

    private final ColdSourceServerService coldSourceServerService;

    private final ColdSourceOverviewService coldSourceOverviewService;

    /**
     * FIELD_MAP 全量映射查询：前端字段 key -> 测点ID数组(tagId[])，null 表示无对应测点
     */
    @GetMapping("/fieldMap")
    @ApiOperation(value = "查询 FIELD_MAP 全量映射", notes = "返回前端字段 key 与测点ID(tagId) 的全部对应关系，null 表示该 key 在点表中无对应测点")
    public Result<Map<String, List<Long>>> getFieldMap() {
        try {
            return Result.ok(coldSourceOverviewService.getFieldMap());
        } catch (Exception e) {
            log.error("查询 FIELD_MAP 映射异常", e);
            return Result.error("查询 FIELD_MAP 映射异常: " + e.getMessage());
        }
    }

    /**
     * 查询服务器连接信息
     */
    @GetMapping("/server/getAllConnectInfo")
    @ApiOperation(value = "查询服务器连接信息", notes = "调用 pSpace serverGetAllConnectInfo")
    public Result<List<PsConnectInfo>> getAllConnectInfo() {
        try {
            PsResult<PsConnectInfo> result = coldSourceServerService.serverGetAllConnectInfo();
            if (result.isSuccess()) {
                return Result.ok(result.getData());
            }
            return Result.error("获取服务器连接信息失败, code=" + result.getCode());
        } catch (Exception e) {
            log.error("获取服务器连接信息异常", e);
            return Result.error("获取服务器连接信息异常: " + e.getMessage());
        }
    }

    /**
     * 查询服务器时间
     */
    @GetMapping("/server/getTime")
    @ApiOperation(value = "查询服务器时间", notes = "调用 pSpace serverGetTime")
    public Result<Long> getTime() {
        try {
            PsResult<Long> result = coldSourceServerService.serverGetTime();
            if (result.isSuccess()) {
                return Result.ok(result.getData().get(0));
            }
            return Result.error("获取服务器时间失败, code=" + result.getCode());
        } catch (Exception e) {
            log.error("获取服务器时间异常", e);
            return Result.error("获取服务器时间异常: " + e.getMessage());
        }
    }

    /**
     * 查询服务器属性（安全区、权限等）
     */
    @GetMapping("/server/getProp")
    @ApiOperation(value = "查询服务器属性", notes = "调用 pSpace serverGetProp")
    public Result<PsServerProp> getProp() {
        try {
            PsResult<PsServerProp> result = coldSourceServerService.serverGetProp();
            if (result.isSuccess()) {
                return Result.ok(result.getData().get(0));
            }
            return Result.error("获取服务器属性失败, code=" + result.getCode());
        } catch (Exception e) {
            log.error("获取服务器属性异常", e);
            return Result.error("获取服务器属性异常: " + e.getMessage());
        }
    }

    /**
     * 更新点位信息数据（单点写值/控制）
     * 请求体: {"tagId": 600, "value": "1"}  或 {"tagId": 600, "value": "25.5"}
     */
    @PostMapping("/realWrite")
    @ApiOperation(value = "更新点位信息数据(单点)", notes = "调用 pSpace realWrite 按测点ID写值")
    public Result<Base> realWrite(@RequestBody ColdSourceWriteDto dto) {
        try {
            if (dto.getTagId() == null || dto.getValue() == null) {
                return Result.error("tagId 和 value 不能为空");
            }
            PsResult<Base> result = coldSourceServerService.realWrite(dto.getTagId(), dto.getValue());
            if (result.isSuccess()) {
                return Result.ok("写点成功");
            }
            return Result.error("写点失败, code=" + result.getCode());
        } catch (Exception e) {
            log.error("冷源写点异常: tagId={}, value={}", dto.getTagId(), dto.getValue(), e);
            return Result.error("冷源写点异常: " + e.getMessage());
        }
    }

    /**
     * 更新点位信息数据（批量写值）
     * 请求体: {"tagIds": [600,601], "values": ["1","25.5"]}
     */
    @PostMapping("/realWriteList")
    @ApiOperation(value = "更新点位信息数据(批量)", notes = "调用 pSpace realWriteList 批量按测点ID写值")
    public Result<Base> realWriteList(@RequestBody ColdSourceWriteDto dto) {
        try {
            if (dto.getTagIds() == null || dto.getValues() == null || dto.getTagIds().isEmpty()
                    || dto.getTagIds().size() != dto.getValues().size()) {
                return Result.error("tagIds 与 values 不能为空且数量必须一致");
            }
            PsResult<Base> result = coldSourceServerService.realWriteList(dto.getTagIds(), dto.getValues());
            if (result.isSuccess()) {
                return Result.ok("批量写点成功");
            }
            return Result.error("批量写点失败, code=" + result.getCode());
        } catch (Exception e) {
            log.error("冷源批量写点异常", e);
            return Result.error("冷源批量写点异常: " + e.getMessage());
        }
    }

}
