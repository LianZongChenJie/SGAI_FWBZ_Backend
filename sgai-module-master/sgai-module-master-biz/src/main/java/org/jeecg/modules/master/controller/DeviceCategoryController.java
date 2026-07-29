package org.jeecg.modules.master.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.master.entity.DeviceCategory;
import org.jeecg.modules.master.service.IDeviceCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Api(tags = "类别主数据")
@RestController
@RequestMapping("/master/deviceCategory")
public class DeviceCategoryController {

    @Autowired
    private IDeviceCategoryService deviceCategoryService;

    @ApiOperation("扁平列表（name 可选模糊）")
    @GetMapping("/list")
    public Result<List<DeviceCategory>> list(@RequestParam(required = false) String name) {
        return Result.OK(deviceCategoryService.listAll(name));
    }

    @ApiOperation("详情")
    @GetMapping("/{id}")
    public Result<DeviceCategory> queryById(@PathVariable("id") String id) {
        return Result.OK(deviceCategoryService.getById(id));
    }

    @ApiOperation("新增")
    @PostMapping
    public Result<?> add(@RequestBody DeviceCategory entity) {
        deviceCategoryService.create(entity);
        return Result.OK("新增成功");
    }

    @ApiOperation("编辑/移动")
    @PutMapping
    public Result<?> edit(@RequestBody DeviceCategory entity) {
        deviceCategoryService.updateNode(entity);
        return Result.OK("编辑成功");
    }

    @ApiOperation("删除")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable("id") String id) {
        deviceCategoryService.removeNode(id);
        return Result.OK("删除成功");
    }

}
