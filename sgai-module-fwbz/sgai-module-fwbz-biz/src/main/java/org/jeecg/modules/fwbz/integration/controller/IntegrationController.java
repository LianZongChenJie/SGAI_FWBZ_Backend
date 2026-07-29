package org.jeecg.modules.fwbz.integration.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.config.shiro.IgnoreAuth;
import org.jeecg.modules.fwbz.integration.dto.IntegrationPayload;
import org.jeecg.modules.fwbz.integration.dto.ReceiveResult;
import org.jeecg.modules.fwbz.integration.service.IntegrationReceiveService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "主数据对接接收")
@RestController
@RequestMapping("/fwbz/integration/receive")
@AllArgsConstructor
public class IntegrationController {

    private IntegrationReceiveService receiveService;

    @ApiOperation("接收仪表（类别/空间/设备，type=1）")
    @IgnoreAuth
    @PostMapping("/meter")
    public Result<ReceiveResult> receiveMeter(@RequestBody IntegrationPayload<Object> payload) {
        return Result.OK("操作成功", receiveService.receive(payload, "1"));
    }

    @ApiOperation("接收设备（类别/空间/设备，type=2）")
    @IgnoreAuth
    @PostMapping("/equipment")
    public Result<ReceiveResult> receiveEquipment(@RequestBody IntegrationPayload<Object> payload) {
        return Result.OK("操作成功", receiveService.receive(payload, "2"));
    }
}
