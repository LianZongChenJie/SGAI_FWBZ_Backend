package org.jeecg.modules.fwbz.energyAnalysis.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.energyAnalysis.entity.CostCenter;
import org.jeecg.modules.fwbz.energyAnalysis.service.ICostCenterService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 成本中心配置
 */
@RestController
@RequestMapping("/fwbz/cost/center")
@AllArgsConstructor
public class CostCenterController {

    private final ICostCenterService service;

//    @RequiresPermissions("Fwbz:cost:center:add")
    @PostMapping("/add")
    public Result<String> add(@RequestBody CostCenter costCenter) {
        service.add(costCenter);
        return Result.ok();
    }

//    @RequiresPermissions("Fwbz:cost:center:update")
    @PostMapping("/update")
    public Result<String> update(@RequestBody CostCenter costCenter) {
        service.update(costCenter);
        return Result.ok();
    }

//    @RequiresPermissions("Fwbz:cost:center:delete")
    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam Long id) {
        service.delete(id);
        return Result.ok();
    }

    @GetMapping("/getTree")
    public Result<List<CostCenter>> getTree() {
        return Result.ok(service.getTree());
    }

    @GetMapping("/list")
    public Result<Page<CostCenter>> list(CostCenter params) {
        return Result.ok(service.listByParentId(params));
    }


}
