package org.jeecg.modules.master.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.util.StrUtil;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.master.entity.IntegrationLog;
import org.jeecg.modules.master.service.IIntegrationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "对接日志")
@RestController
@RequestMapping("/master/integrationLog")
public class IntegrationLogController {

    @Autowired
    private IIntegrationLogService integrationLogService;

    @ApiOperation("分页列表")
    @GetMapping("/list")
    public Result<Page<IntegrationLog>> list(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String systemId,
            @RequestParam(required = false) String systemCode,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        Page<IntegrationLog> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<IntegrationLog> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(direction)) {
            w.eq(IntegrationLog::getDirection, direction);
        }
        if (StrUtil.isNotBlank(systemId)) {
            w.eq(IntegrationLog::getSystemId, systemId);
        }
        if (StrUtil.isNotBlank(systemCode)) {
            w.eq(IntegrationLog::getSystemCode, systemCode);
        }
        if (StrUtil.isNotBlank(type)) {
            w.eq(IntegrationLog::getType, type);
        }
        if (StrUtil.isNotBlank(status)) {
            w.eq(IntegrationLog::getStatus, status);
        }
        w.orderByDesc(IntegrationLog::getCreateTime);
        return Result.OK(integrationLogService.page(page, w));
    }

    @ApiOperation("详情（含 payload 原文）")
    @GetMapping("/{id}")
    public Result<IntegrationLog> queryById(@PathVariable("id") String id) {
        return Result.OK(integrationLogService.getById(id));
    }
}
