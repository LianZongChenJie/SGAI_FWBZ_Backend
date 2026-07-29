package org.jeecg.modules.master.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.master.entity.IntegrationSystem;
import org.jeecg.modules.master.service.IIntegrationPushService;
import org.jeecg.modules.master.service.IIntegrationSystemService;
import org.jeecg.modules.master.vo.IntegrationSystemForm;
import org.jeecg.modules.master.vo.PushSnapshotResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Api(tags = "对接系统")
@RestController
@RequestMapping("/master/integrationSystem")
public class IntegrationSystemController {

    @Autowired
    private IIntegrationSystemService integrationSystemService;
    @Autowired
    private IIntegrationPushService integrationPushService;

    @ApiOperation("分页列表")
    @GetMapping("/list")
    public Result<IPage<IntegrationSystem>> list(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code) {
        Page<IntegrationSystem> page = new Page<>(pageNo, pageSize);
        return Result.OK(integrationSystemService.listPage(page, name, code));
    }

    @ApiOperation("详情（含类别范围 categoryIds）")
    @GetMapping("/{id}")
    public Result<IntegrationSystemForm> queryById(@PathVariable("id") String id) {
        return Result.OK(integrationSystemService.getFormById(id));
    }

    @ApiOperation("新增")
    @PostMapping
    public Result<?> add(@RequestBody IntegrationSystemForm form) {
        integrationSystemService.saveFromForm(form);
        return Result.OK("新增成功");
    }

    @ApiOperation("编辑")
    @PutMapping
    public Result<?> edit(@RequestBody IntegrationSystemForm form) {
        integrationSystemService.updateFromForm(form);
        return Result.OK("编辑成功");
    }

    @ApiOperation("删除（须先停用）")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable("id") String id) {
        integrationSystemService.removeByIdWithCheck(id);
        return Result.OK("删除成功");
    }

    @ApiOperation("手动全量推送（3 次快照：空间/类别/设备）")
    @PostMapping("/{id}/push")
    public Result<List<PushSnapshotResult>> push(@PathVariable("id") String id) {
        return Result.OK(integrationPushService.pushSnapshotForSystem(id));
    }
}
