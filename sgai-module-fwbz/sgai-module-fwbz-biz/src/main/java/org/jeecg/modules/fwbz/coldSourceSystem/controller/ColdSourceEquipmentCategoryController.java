package org.jeecg.modules.fwbz.coldSourceSystem.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.coldSourceSystem.entity.ColdSourceEquipmentCategory;
import org.jeecg.modules.fwbz.coldSourceSystem.mapper.ColdSourceEquipmentCategoryMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 冷源设备类别
 */
@RestController
@RequestMapping("/fwbz/coldSource/category")
@AllArgsConstructor
@Slf4j
@Api(tags = "冷源设备类别")
public class ColdSourceEquipmentCategoryController {

    private final ColdSourceEquipmentCategoryMapper coldSourceEquipmentCategoryMapper;

    /**
     * 查询冷源设备类别列表
     * 条件: categoryName(类别名称, 模糊) / type(类别类型, 精确: 1计量 2楼控) / pid(父级id, 精确)
     */
    @GetMapping("/list")
    @ApiOperation(value = "查询冷源设备类别列表", notes = "条件: categoryName(类别名称模糊)/type(类别类型精确: 1计量 2楼控)/pid(父级id精确)")
    public Result<List<ColdSourceEquipmentCategory>> queryCategoryList(@RequestParam(required = false) String categoryName,
                                                                        @RequestParam(required = false) Integer type,
                                                                        @RequestParam(required = false) Long pid) {
        try {
            return Result.ok(coldSourceEquipmentCategoryMapper.selectCategoryList(categoryName, type, pid));
        } catch (Exception e) {
            log.error("查询冷源设备类别列表异常", e);
            return Result.error("查询冷源设备类别列表异常: " + e.getMessage());
        }
    }
}
