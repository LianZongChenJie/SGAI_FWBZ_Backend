package org.jeecg.modules.fwbz.energyAnalysis.controller;

import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.energyAnalysis.service.IEnergyMeteringService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fwbz/energyMetering")
@AllArgsConstructor
@Api(tags="能源计量-概览")
@Slf4j
public class EnergyMeteringController {

    private final IEnergyMeteringService service;


    /**
     * 状态统计
     * @return 统计结果
     */
    @GetMapping("/statistics")
    public Result<?> deviceRunStateStatistics(){
        return Result.ok(service.statistics());
    }


}
