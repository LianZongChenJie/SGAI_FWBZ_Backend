package org.jeecg.modules.fwbz.patterned.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.patterned.entity.PatterningExecutionTime;
import org.jeecg.modules.fwbz.patterned.entity.PatterningStrategy;
import org.jeecg.modules.fwbz.patterned.service.IPatterningStrategyService;
import org.springframework.web.bind.annotation.*;

/**
 * 场景控制
 */
@RestController
@RequestMapping("/Fwbz/patterningStrategy")
@AllArgsConstructor
public class PatterningStrategyController {

    private final IPatterningStrategyService service;

    @AutoLog(value = "场景控制-添加")
    @RequiresPermissions("Fwbz:patterningStrategy:add")
    @PostMapping("/add")
    public Result<String> add(@RequestBody PatterningStrategy entity){
        service.save(entity);
        return Result.ok();
    }

    @AutoLog(value = "场景控制-编辑")
    @RequiresPermissions("Fwbz:patterningStrategy:edit")
    @PostMapping("/edit")
    public Result<String> edit(@RequestBody PatterningStrategy entity){
        service.updateById(entity);
        return Result.ok();
    }

    @GetMapping("/getDetailById")
    public Result<PatterningStrategy> getDetailById(Long id){
        return Result.ok(service.getDetailById(id));
    }

    @AutoLog(value = "场景控制-删除")
    @RequiresPermissions("Fwbz:patterningStrategy:delete")
    @DeleteMapping("/delete")
    public Result<String> delete(Long id){
        service.deleteById(id);
        return Result.ok();
    }

    @AutoLog(value = "场景控制-启用")
    @RequiresPermissions("Fwbz:patterningStrategy:startStrategy")
    @PostMapping("/startStrategy")
    public Result<String> startStrategy(@RequestBody PatterningExecutionTime data){
        service.startStrategy(data);
        return Result.ok();
    }

    @AutoLog(value = "场景控制-禁用")
    @RequiresPermissions("Fwbz:patterningStrategy:stopStrategy")
    @PostMapping("/stopStrategy")
    public Result<String> stopStrategy(@RequestParam Long id){
        service.stopStrategy(id);
        return Result.ok();
    }

    @AutoLog(value = "场景控制-立即执行")
//    @RequiresPermissions("Fwbz:patterningStrategy:executionNow")
    @PostMapping("/executionNow")
    public Result<String> executionNow(@RequestParam Long id){
        service.executeImmediately(id);
        return Result.ok();
    }

    @GetMapping("/listPage")
    public Result<Page<PatterningStrategy>> listPage(PatterningStrategy params){
        return Result.ok(service.listPage(params));
    }
}
