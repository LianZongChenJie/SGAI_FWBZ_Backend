package org.jeecg.modules.fwbz.hikvision.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.hikvision.dto.EventNotifyPushRequest;
import org.jeecg.modules.fwbz.hikvision.service.IEventNotifyService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 海康事件通知接收控制器
 * <p>接收海康平台推送的事件通知，解析后存入数据库。</p>
 * <p>海康平台通过HTTP POST方式推送到此接口，JSON格式参考 {@link EventNotifyPushRequest}</p>
 *
 * @author fwbz
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/fwbz/hikvision/eventNotify")
@Api(tags = "海康事件通知接收")
public class EventNotifyController {

    private final IEventNotifyService eventNotifyService;

    /**
     * 接收海康事件推送
     * <p>海康平台将根据订阅的事件类型，实时推送事件到此接口。此接口必须返回HTTP 200，
     * 否则海康会认为推送失败并进行重试。</p>
     *
     * @param pushRequest 海康推送的事件JSON
     * @return 处理结果
     */
    @PostMapping("/receive")
    @ApiOperation(value = "接收海康事件推送", notes = "海康平台实时推送事件到此接口，解析并存入数据库")
    public Result<String> receiveEvent(@RequestBody EventNotifyPushRequest pushRequest) {
        try {
            int savedCount = eventNotifyService.handleEventNotify(pushRequest);
            log.info("海康事件推送处理完成, 共保存{}条事件", savedCount);
            // 必须返回成功，否则海康会重试推送
            return Result.ok("成功接收并保存 " + savedCount + " 条事件");
        } catch (Exception e) {
            log.error("处理海康事件推送异常", e);
            // 即使处理异常也返回200，避免海康无限重试
            return Result.error("处理事件推送失败: " + e.getMessage());
        }
    }
}
