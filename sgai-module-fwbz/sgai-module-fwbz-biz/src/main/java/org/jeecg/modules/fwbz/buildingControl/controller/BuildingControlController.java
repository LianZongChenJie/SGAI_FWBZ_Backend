package org.jeecg.modules.fwbz.buildingControl.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.buildingControl.dto.UpdRealDataItemDto;
import org.jeecg.modules.fwbz.buildingControl.dto.UpdRealDataResponse;
import org.jeecg.modules.fwbz.buildingControl.service.BuildingControlService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 楼控系统控制接口
 */
@RestController
@RequestMapping("/fwbz/buildingControl")
@AllArgsConstructor
@Slf4j
@Api(tags = "楼控系统")
@Validated
public class BuildingControlController {

    private final BuildingControlService buildingControlService;

    /**
     * 实时数据写入外部系统
     *
     * @param items 更新项列表（tagid + 设定值）
     * @return 外部系统返回结果
     */
    @PostMapping("/updRealData")
    @ApiOperation(value = "实时数据写入", notes = "接收前端 tagid 与设定值，调用外部系统 PUT /UpdRealData 接口写入实时数据")
    public Result<UpdRealDataResponse> updRealData(@RequestBody @Valid List<UpdRealDataItemDto> items) {
        try {
            UpdRealDataResponse response = buildingControlService.updRealData(items);
            return Result.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("实时数据写入参数校验失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("实时数据写入异常", e);
            return Result.error("实时数据写入异常: " + e.getMessage());
        }
    }
}
