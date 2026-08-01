package org.jeecg.modules.fwbz.energyAnalysis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.energyAnalysis.service.IEenergyMeteringService;
import org.jeecg.modules.fwbz.mdm.entity.Device;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fwbz/meteringPoint")
@AllArgsConstructor
@Api(tags="能源计量-概览")
@Slf4j
public class EnergyMeteringController {

    private final IEenergyMeteringService service;

//    @AutoLog(value = "设备基础信息-设备添加")
    @ApiOperation(value="能源计量-计量表计数据", notes="能源计量-计量表计数据")
    @PostMapping("/deviceMeterData/list")
    public Result<String> deviceMeterDataList(@RequestBody Device device){
        service.deviceMeterDataList(device);
        return Result.OK("添加成功！");
    }


}
