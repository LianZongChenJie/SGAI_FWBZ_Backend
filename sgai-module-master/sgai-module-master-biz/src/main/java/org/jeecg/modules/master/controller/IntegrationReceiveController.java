package org.jeecg.modules.master.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.config.shiro.IgnoreAuth;
import org.jeecg.modules.master.service.IIntegrationReceiveService;
import org.jeecg.modules.master.vo.ReceivePayload;
import org.jeecg.modules.master.vo.ReceiveResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "数据对接-接收")
@RestController
@RequestMapping("/master/integration")
public class IntegrationReceiveController {

    @Autowired
    private IIntegrationReceiveService receiveService;

    @AutoLog(value="接收外部设备推送")
    @ApiOperation("接收外部设备推送（令牌鉴权）")
    @IgnoreAuth
    @PostMapping("/receive")
    public ResponseEntity<Result<ReceiveResult>> receive(
            @RequestHeader(value = "X-Integration-Token", required = false) String token,
            @RequestBody ReceivePayload payload) {
        try {
            return ResponseEntity.ok(Result.OK(receiveService.receive(payload, token)));
        } catch (JeecgBootException e) {
            // 仅鉴权失败会抛到此处（逐条业务异常已在 service 内 catch）
            return ResponseEntity.status(401).body(Result.error(e.getMessage()));
        }
    }
}
