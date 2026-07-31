package org.jeecg.modules.fwbz.patterned.controller;

import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.patterned.entity.PatterningExecutionTime;
import org.jeecg.modules.fwbz.patterned.service.IPatterningExecutionTimeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Fwbz/patterningExecutionTime")
@AllArgsConstructor
public class PatterningExecutionTimeController {

    private final IPatterningExecutionTimeService service;

    @GetMapping("/getById")
    public Result<PatterningExecutionTime> getByPatterningIdId(@RequestParam Long patterningId){
        return Result.ok(service.getByPatterningId(patterningId));
    }
}
