package org.jeecg.modules.fwbz.buildingControl.controller;

import com.sunwayland.pspace.entity.PsData;
import com.sunwayland.pspace.entity.PsResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.buildingControl.dto.UpdRealDataItemDto;
import org.jeecg.modules.fwbz.buildingControl.dto.UpdRealDataResponse;
import org.jeecg.modules.fwbz.buildingControl.dto.UpdateValueTypeResponseDto;
import org.jeecg.modules.fwbz.buildingControl.service.BuildingControlService;
import org.jeecg.modules.fwbz.coldSourceSystem.service.ColdSourceServerService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    private final ColdSourceServerService coldSourceServerService;

    /**
     * 实时数据写入外部系统
     *
     * @param items 更新项列表（tagid + 设定值）
     * @return 外部系统返回结果
     */
    @PostMapping("/updRealData")
    @ApiOperation(value = "实时数据写入", notes = "接收前端 tagid 与设定值，调用外部系统 PUT /UpdRealData 接口写入实时数据")
    public Result<UpdRealDataResponse> updRealData(@RequestBody @Valid UpdRealDataItemDto items) {
        try {
            String  response = buildingControlService.updRealData(items);
            return Result.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("实时数据写入参数校验失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("实时数据写入异常", e);
            return Result.error("实时数据写入异常: " + e.getMessage());
        }
    }

    /**
     * 读取楼控点位真实值
     *
     * @param tagId 测点ID（对应 device_attribute.acquisition_coding）
     * @return 点位真实值
     */
    @GetMapping("/realRead")
    @ApiOperation(value = "读取点位真实值", notes = "根据tagId读取楼控点位当前真实值")
    public Result<Object> realRead(@ApiParam(value = "测点ID", required = true) @RequestParam Long tagId) {
        try {
            PsResult<PsData> result = coldSourceServerService.connect().realRead(tagId);
            if (result.isSuccess()) {
                List<PsData> dataList = result.getData();
                if (dataList != null && !dataList.isEmpty()) {
                    return Result.OK(dataList.get(0));
                }
                return Result.error("读取点位真实值返回数据为空");
            } else {
                log.warn("读取点位真实值失败: tagId={}, code={}", tagId, result.getCode());
                return Result.error("读取点位真实值失败: " + result.getCode());
            }
        } catch (IllegalArgumentException e) {
            log.warn("读取点位真实值参数校验失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("读取点位真实值异常: tagId={}", tagId, e);
            return Result.error("读取点位真实值异常: " + e.getMessage());
        }
    }

    /**
     * 更新设备属性数据类型：读取 device_attribute 中采集编码为数字的所有属性，
     * 逐点 realRead 获取返回的 dataType，回写 value_type。
     */
    @PostMapping("/updateValueType")
    @ApiOperation(value = "更新设备属性数据类型", notes = "遍历采集编码为数字的属性，通过 realRead 读取 dataType 并回写 value_type")
    public Result<UpdateValueTypeResponseDto> updateValueType() {
        try {
            return Result.ok(buildingControlService.updateAttributeValueType());
        } catch (Exception e) {
            log.error("更新设备属性数据类型异常", e);
            return Result.error("更新设备属性数据类型异常: " + e.getMessage());
        }
    }
}
