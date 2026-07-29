package org.jeecg.modules.fwbz.activeMeet.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.activeMeet.entity.ActiveMeetInfo;
import org.jeecg.modules.fwbz.activeMeet.service.IActiveMeetInfoService;
import org.jeecg.modules.fwbz.activeMeet.vo.WeekActivityVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fwbz/activeMeet/info")
@AllArgsConstructor
public class ActiveMeetInfoController {

    private final IActiveMeetInfoService service;

    @PostMapping("/add")
    //@RequiresPermissions("fwbz:activeMeetInfo:add")
    @AutoLog(value = "活动信息-添加")
    public Result<String> add(@RequestBody ActiveMeetInfo param) {
        service.save(param);
        return Result.ok();
    }

    @PostMapping("/edit")
    @RequiresPermissions("fwbz:activeMeetInfo:edit")
    @AutoLog(value = "活动信息-编辑")
    public Result<String> edit(@RequestBody ActiveMeetInfo param) {
        service.updateById(param);
        return Result.ok();
    }

    @DeleteMapping("/delete")
    @RequiresPermissions("fwbz:activeMeetInfo:delete")
    @AutoLog(value = "活动信息-删除")
    public Result<String> delete(@RequestParam(name = "id") Long id) {
        service.removeById(id);
        return Result.ok();
    }

    @GetMapping("/listPage")
    public Result<IPage<ActiveMeetInfo>> listPage(ActiveMeetInfo params) {
        return Result.ok(service.listPage(params));
    }

    @GetMapping("/list")
    public Result<List<ActiveMeetInfo>> list() {
        return Result.ok(service.listAll());
    }

    @GetMapping("/thisWeek")
    public Result<List<WeekActivityVO>> thisWeek() {
        return Result.ok(service.listThisWeek());
    }
}
