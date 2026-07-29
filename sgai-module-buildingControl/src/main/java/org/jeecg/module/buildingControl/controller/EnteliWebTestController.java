package org.jeecg.module.buildingControl.controller;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.config.shiro.IgnoreAuth;
import org.jeecg.module.buildingControl.job.DataCollectJob;
import org.jeecg.module.buildingControl.mq.MqSendService;
import org.jeecg.module.buildingControl.service.EnteliWebService;
import org.jeecg.module.buildingControl.util.BacnetPropertyResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/test/enteliweb")
public class EnteliWebTestController {

    @Autowired
    private EnteliWebService enteliWebService;
    @Autowired
    private DataCollectJob job;

    @Autowired
    private MqSendService mqSendService;

    @IgnoreAuth
    @GetMapping("/property")
    public Result<?> getProperty(@RequestParam String path) {
        try {
            BacnetPropertyResult propertyWithType = enteliWebService.getPropertyWithType(path);
            mqSendService.sendMsg(path,propertyWithType.getValue(), LocalDateTime.now());
            return Result.ok(propertyWithType);
        } catch (Exception e) {
            log.error("获取属性值失败", e);
            return Result.error(e.getMessage());
        }
    }

    @IgnoreAuth
    @PostMapping("/property")
    public Result<?> setProperty(@RequestParam String path, @RequestParam String value) {
        try {
            boolean success = enteliWebService.setProperty(path, value);
            return Result.ok();
        } catch (Exception e) {
            log.error("设置属性值失败", e);
            return Result.error(e.getMessage());
        }
    }

    @IgnoreAuth
    @GetMapping("/test")
    public Result<?> test(){
        job.collect();
        return Result.ok();
    }
}
