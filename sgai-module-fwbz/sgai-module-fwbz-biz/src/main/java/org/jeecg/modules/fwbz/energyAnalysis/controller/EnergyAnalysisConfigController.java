package org.jeecg.modules.fwbz.energyAnalysis.controller;

import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.energyAnalysis.entity.EnergyAnalysisConfig;
import org.jeecg.modules.fwbz.energyAnalysis.service.IEnergyAnalysisConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 能效分析配置
 */
@RestController
@RequestMapping("/fwbz/energyAnalysis/config")
@AllArgsConstructor
public class EnergyAnalysisConfigController{
    private final IEnergyAnalysisConfigService service;

    /**
     * 添加
     */
    @RequiresPermissions("fwbz:energyAnalysisConfig:add")
    @PostMapping("/add")
    public Result<String> add(@RequestBody EnergyAnalysisConfig data){
        service.add(data);
        return Result.OK("添加成功！");
    }

    /**
     * 修改
     */
    @RequiresPermissions("fwbz:energyAnalysisConfig:update")
    @PostMapping("/update")
    public Result<String> update(@RequestBody EnergyAnalysisConfig data){
        service.update(data);
        return Result.OK("修改成功！");
    }

    /**
     * 启用
     */
    @RequiresPermissions("fwbz:energyAnalysisConfig:enable")
    @PostMapping("/enable")
    public Result<String> enable(@RequestParam Long id){
        service.enable(id);
        return Result.OK("启用成功！");
    }

    /**
     * 禁用
     * @param id 配置id
     */
    @RequiresPermissions("fwbz:energyAnalysisConfig:disable")
    @PostMapping("/disable")
    public Result<String> disable(@RequestParam Long id){
        service.disable(id);
        return Result.OK("禁用成功！");
    }

    @GetMapping("/list")
    public Result<List<EnergyAnalysisConfig>> list(EnergyAnalysisConfig params){
        return Result.ok(service.list(params));
    }

}
