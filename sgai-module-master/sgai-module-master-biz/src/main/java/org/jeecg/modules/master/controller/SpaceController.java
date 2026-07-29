package org.jeecg.modules.master.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.master.entity.Space;
import org.jeecg.modules.master.service.ISpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Api(tags = "空间主数据")
@RestController
@RequestMapping("/master/space")
public class SpaceController {

    @Autowired
    private ISpaceService spaceService;

    @ApiOperation("扁平列表（name 可选模糊）")
    @GetMapping("/list")
    public Result<List<Space>> list(@RequestParam(required = false) String name) {
        return Result.OK(spaceService.listAll(name));
    }

    @ApiOperation("详情")
    @GetMapping("/{id}")
    public Result<Space> queryById(@PathVariable("id") String id) {
        return Result.OK(spaceService.getById(id));
    }

    @ApiOperation("新增")
    @PostMapping
    public Result<?> add(@RequestBody Space entity) {
        spaceService.create(entity);
        return Result.OK("新增成功");
    }

    @ApiOperation("编辑/移动")
    @PutMapping
    public Result<?> edit(@RequestBody Space entity) {
        spaceService.updateNode(entity);
        return Result.OK("编辑成功");
    }

    @ApiOperation("删除")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable("id") String id) {
        spaceService.removeNode(id);
        return Result.OK("删除成功");
    }
}
