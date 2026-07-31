package org.jeecg.modules.fwbz.dataInterface.controller;

import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.dataInterface.entity.ProtocolTypeInfo;
import org.jeecg.modules.fwbz.dataInterface.service.IProtocolTypeInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 接口协议类型 Controller（提供下拉列表）
 */
@RestController
@RequestMapping("/fwbz/protocolType")
@AllArgsConstructor
public class ProtocolTypeInfoController {

    private final IProtocolTypeInfoService service;

    /**
     * 查询全部协议类型（下拉列表）
     */
    @GetMapping("/list")
    public Result<List<ProtocolTypeInfo>> list() {
        return Result.ok(service.list());
    }
}
