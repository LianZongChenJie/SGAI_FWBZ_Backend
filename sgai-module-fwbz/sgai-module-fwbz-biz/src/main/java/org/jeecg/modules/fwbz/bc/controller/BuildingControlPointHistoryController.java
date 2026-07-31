package org.jeecg.modules.fwbz.bc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.bc.dto.BuildingControlPointDto;
import org.jeecg.modules.fwbz.bc.entity.BuildingControlPointHistory;
import org.jeecg.modules.fwbz.bc.service.IBuildingControlPointHistoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 楼控点位历史值
 */
@RestController
@RequestMapping("/fwbz/bc/buildingControlPointHistory")
@AllArgsConstructor
public class BuildingControlPointHistoryController {

    private final IBuildingControlPointHistoryService service;

    @GetMapping("/listPage")
    private Result<Page<BuildingControlPointHistory>> listPage(BuildingControlPointDto params){
        return Result.ok(service.listPage(params));
    }
}
