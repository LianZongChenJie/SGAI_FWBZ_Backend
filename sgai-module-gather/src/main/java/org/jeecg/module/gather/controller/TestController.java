package org.jeecg.module.gather.controller;

import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.config.shiro.IgnoreAuth;
import org.jeecg.module.gather.job.GatherJob;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gather")
@AllArgsConstructor
public class TestController {

    private final GatherJob job;

    @IgnoreAuth
    @GetMapping("/runStatus")
    public Result<String> runStatus(){
        job.deviceStatusGather();
        return Result.ok();
    }

    @IgnoreAuth
    @GetMapping("/energyData")
    public Result<String> energyData(){
        job.deviceDataGather();
        return Result.ok();
    }
}
