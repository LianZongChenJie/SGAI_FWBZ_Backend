package org.jeecg.modules.fwbz.coldSourceSystem.controller;

import com.sunwayland.pspace.entity.PsConnectInfo;
import com.sunwayland.pspace.entity.PsResult;
import com.sunwayland.pspace.entity.PsServerProp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.coldSourceSystem.service.ColdSourceServerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 冷源系统(pSpace) —— 服务器信息查询接口
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
