package org.jeecg.modules.fwbz.alarm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.alarm.entity.AlarmCategory;
import org.jeecg.modules.fwbz.alarm.service.IAlarmCategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fwbz/alarm/category")
@AllArgsConstructor
public class AlarmCategoryController {

    private final IAlarmCategoryService service;

    @PostMapping("/add")
    @RequiresPermissions("fwbz:alarmCategory:add")
    @AutoLog(value = "告警类别-添加")
    public Result<String> add(@RequestBody AlarmCategory param) {
        service.save(param);
        return Result.ok();
    }

    @PostMapping("/edit")
    @RequiresPermissions("fwbz:alarmCategory:edit")
    @AutoLog(value = "告警类别-编辑")
    public Result<String> edit(@RequestBody AlarmCategory param) {
        service.updateById(param);
        return Result.ok();
    }

    @DeleteMapping("/delete")
    @RequiresPermissions("fwbz:alarmCategory:delete")
    @AutoLog(value = "告警类别-删除")
    public Result<String> delete(@RequestParam(name = "id") Long id) {
        service.removeById(id);
        return Result.ok();
    }

    @PostMapping("/startCategory")
    @RequiresPermissions("fwbz:alarmCategory:startCategory")
    @AutoLog(value = "告警类别-启用")
    public Result<String> startCategory(@RequestParam(name = "id") Long id) {
        service.startCategory(id);
        return Result.ok();
    }

    @PostMapping("/stopCategory")
    @RequiresPermissions("fwbz:alarmCategory:stopCategory")
    @AutoLog(value = "告警类别-停用")
    public Result<String> stopCategory(@RequestParam(name = "id") Long id) {
        service.stopCategory(id);
        return Result.ok();
    }

    @GetMapping("/listPage")
    public Result<IPage<AlarmCategory>> listPage(AlarmCategory params) {
        return Result.ok(service.listPage(params));
    }

    @GetMapping("/list")
    public Result<List<AlarmCategory>> list() {
        return Result.ok(service.list());
    }
}
