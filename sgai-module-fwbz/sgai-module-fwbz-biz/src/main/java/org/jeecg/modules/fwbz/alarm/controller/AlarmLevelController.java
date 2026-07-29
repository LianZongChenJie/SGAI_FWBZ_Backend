package org.jeecg.modules.fwbz.alarm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.alarm.entity.AlarmLevel;
import org.jeecg.modules.fwbz.alarm.service.IAlarmLevelService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fwbz/alarm/level")
@AllArgsConstructor
public class AlarmLevelController {

    private final IAlarmLevelService service;

    @PostMapping("/add")
    @RequiresPermissions("fwbz:alarmLevel:add")
    @AutoLog(value = "报警级别-添加")
    public Result<String> add(@RequestBody AlarmLevel param){
        service.save(param);
        return Result.ok();
    }

    @PostMapping("/edit")
    @RequiresPermissions("fwbz:alarmLevel:edit")
    @AutoLog(value = "报警级别-编辑")
    public Result<String> edit(@RequestBody AlarmLevel param){
        service.updateById(param);
        return Result.ok();
    }

    @DeleteMapping("/delete")
    @RequiresPermissions("fwbz:alarmLevel:delete")
    @AutoLog(value = "报警级别-删除")
    public Result<String> delete(Long id){
        service.removeById(id);
        return Result.ok();
    }

    @GetMapping("/listPage")
    public Result<IPage<AlarmLevel>> listPage(AlarmLevel params){
        return Result.ok(service.listPage(params));
    }

    @GetMapping("/list")
    public Result<List<AlarmLevel>> list(){
        return Result.ok(service.list());
    }

    @PostMapping("/startLevel")
    @RequiresPermissions("fwbz:alarmLevel:startLevel")
    @AutoLog(value = "报警级别-启用")
    public Result<String> startLevel(@RequestParam(name = "id") Long id){
        service.startLevel(id);
        return Result.ok();
    }

    @PostMapping("/stopLevel")
    @RequiresPermissions("fwbz:alarmLevel:stopLevel")
    @AutoLog(value = "报警级别-禁用")
    public Result<String> stopLevel(@RequestParam(name = "id") Long id){
        service.stopLevel(id);
        return Result.ok();
    }

}
