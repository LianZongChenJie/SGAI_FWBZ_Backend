package org.jeecg.modules.fwbz.dataInterface.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.dataInterface.entity.InterfaceInfo;
import org.jeecg.modules.fwbz.dataInterface.service.IInterfaceInfoService;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 接口信息 Controller
 */
@RestController
@RequestMapping("/fwbz/interfaceInfo")
@AllArgsConstructor
public class InterfaceInfoController {

    private final IInterfaceInfoService service;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class,
                new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"), true));
    }

    /**
     * 添加
     */
    @PostMapping("/add")
    @RequiresPermissions("fwbz:interfaceInfo:add")
    @AutoLog(value = "接口信息-添加")
    public Result<String> add(@RequestBody InterfaceInfo param) {
        service.save(param);
        return Result.ok();
    }

    /**
     * 编辑
     */
    @PostMapping("/edit")
    @RequiresPermissions("fwbz:interfaceInfo:edit")
    @AutoLog(value = "接口信息-编辑")
    public Result<String> edit(@RequestBody InterfaceInfo param) {
        service.updateById(param);
        return Result.ok();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete")
    @RequiresPermissions("fwbz:interfaceInfo:delete")
    @AutoLog(value = "接口信息-删除")
    public Result<String> delete(@RequestParam(name = "id") Long id) {
        service.removeById(id);
        return Result.ok();
    }

    /**
     * 启用监控
     */
    @PostMapping("/enable")
    //@RequiresPermissions("fwbz:interfaceInfo:enable")
    @AutoLog(value = "接口信息-启用监控")
    public Result<String> enable(@RequestParam(name = "id") Long id) {
        service.enable(id);
        return Result.ok();
    }

    /**
     * 停用监控
     */
    @PostMapping("/disable")
    //@RequiresPermissions("fwbz:interfaceInfo:disable")
    @AutoLog(value = "接口信息-停用监控")
    public Result<String> disable(@RequestParam(name = "id") Long id) {
        service.disable(id);
        return Result.ok();
    }

    /**
     * 分页查询
     */
    @GetMapping("/listPage")
    public Result<IPage<InterfaceInfo>> listPage(InterfaceInfo params) {
        return Result.ok(service.listPage(params));
    }

    /**
     * 查询全部（启用状态）
     */
    @GetMapping("/list")
    public Result<List<InterfaceInfo>> list() {
        return Result.ok(service.list());
    }
}
