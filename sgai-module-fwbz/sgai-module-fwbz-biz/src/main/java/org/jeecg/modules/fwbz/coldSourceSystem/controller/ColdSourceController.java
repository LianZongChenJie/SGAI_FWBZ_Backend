package org.jeecg.modules.fwbz.coldSourceSystem.controller;

import com.sunwayland.pspace.entity.PsConnectInfo;
import com.sunwayland.pspace.entity.PsResult;
import com.sunwayland.pspace.entity.PsServerProp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.coldSourceSystem.entity.RealDataResp;
import org.jeecg.modules.fwbz.coldSourceSystem.service.ColdSourceOverviewService;
import org.jeecg.modules.fwbz.coldSourceSystem.service.ColdSourceServerService;
import org.jeecg.modules.fwbz.coldSourceSystem.service.RealDataApiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 冷源系统(pSpace) —— 服务器信息查询 + 实时数据接口
 *
 * 对应 SDK 自带 ServerAPIDemo 的 serverGetAllConnectInfo / serverGetTime / serverGetProp
 */
@RestController
@RequestMapping("/fwbz/coldSource")
@AllArgsConstructor
@Slf4j
@Api(tags = "冷源系统")
public class ColdSourceController {

    private final ColdSourceServerService coldSourceServerService;

    private final RealDataApiService realDataApiService;

    private final ColdSourceOverviewService coldSourceOverviewService;

    /**
     * 冷源系统总览数据：按前端 centralized-water 数据结构组装实时值
     * 测点ID由 FIELD_MAP（key->tagId）直接维护，值批量调用 /RealData 获取
     */
    @GetMapping("/overview")
    @ApiOperation(value = "冷源系统总览数据", notes = "按前端数据结构组装实时值，点ID由点表描述映射，值通过 pSpace /RealData 获取")
    public Result<Map<String, Object>> getOverview() {
        try {
            Map<String, Object> data = coldSourceOverviewService.buildOverview();
            log.info("冷源总览数据组装完成, 字段数={}", data.size());
            return Result.ok(data);
        } catch (Exception e) {
            log.error("组装冷源总览数据异常", e);
            return Result.error("组装冷源总览数据异常: " + e.getMessage());
        }
    }

    /**
     * 获取实时数据（对应 pSpace WebApi 文档 [RealData]）
     * tagids 与 tagnames 至少传一个，逗号分隔
     */
    @GetMapping("/realData")
    @ApiOperation(value = "获取实时数据", notes = "调用 pSpace WebApi /RealData，tagids/tagnames 逗号分隔，至少传一个")
    public Result<RealDataResp> getRealData(@RequestParam(required = false) String tagids,
                                            @RequestParam(required = false) String tagnames,
                                            @RequestParam(required = false, defaultValue = "1") Integer timetype) {
        try {
            if ((tagids == null || tagids.trim().isEmpty()) && (tagnames == null || tagnames.trim().isEmpty())) {
                return Result.error("tagids 与 tagnames 至少传一个");
            }
            RealDataResp resp = realDataApiService.getRealData(tagids, tagnames, timetype);
            if (resp.isSuccess()) {
                return Result.ok(resp);
            }
            return Result.error("获取实时数据失败, code=" + resp.getCode() + ", mesg=" + resp.getMesg());
        } catch (Exception e) {
            log.error("获取实时数据异常", e);
            return Result.error("获取实时数据异常: " + e.getMessage());
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

}
