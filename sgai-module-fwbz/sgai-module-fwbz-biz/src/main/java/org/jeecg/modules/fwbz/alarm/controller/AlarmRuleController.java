package org.jeecg.modules.fwbz.alarm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.alarm.entity.AlarmRules;
import org.jeecg.modules.fwbz.alarm.service.IAlarmRulesService;
import org.springframework.web.bind.annotation.*;

/**
 * 告警规则
 */
@RestController
@RequestMapping("/fwbz/alarm/rule")
@AllArgsConstructor
public class AlarmRuleController {

    private final IAlarmRulesService service;

    @PostMapping("/add")
    @RequiresPermissions("Fwbz:alarmRule:add")
    @AutoLog(value = "告警规则-新增")
    public Result<String> add(@RequestBody AlarmRules data){
        service.save(data);
        return Result.ok();
    }

    @PostMapping("/edit")
    @RequiresPermissions("Fwbz:alarmRule:edit")
    @AutoLog(value = "告警规则-编辑")
    public Result<String> edit(@RequestBody AlarmRules data){
        service.updateById(data);
        return Result.ok();
    }

    @DeleteMapping("/delete")
    @RequiresPermissions("Fwbz:alarmRule:delete")
    @AutoLog(value = "告警规则-删除")
    public Result<String> delete(Long id){
        service.removeById(id);
        return Result.ok();
    }

    @GetMapping("/getDetailById")
    public Result<AlarmRules> getDetailById(Long id){
        return Result.ok(service.getDetailById(id));
    }

    @PostMapping("/startRule")
    @RequiresPermissions("Fwbz:alarmRule:startRule")
    @AutoLog(value = "告警规则-启用")
    public Result<String> startRule(Long id){
        service.startRule(id);
        return Result.ok();
    }

    @PostMapping("/stopRule")
    @RequiresPermissions("Fwbz:alarmRule:stopRule")
    @AutoLog(value = "告警规则-禁用")
    public Result<String> stopRule(Long id){
        service.stopRule(id);
        return Result.ok();
    }


    @GetMapping("/listPage")
    public Result<IPage<AlarmRules>> listPage(AlarmRules params){
        return Result.ok(service.listPage(params));
    }


}
