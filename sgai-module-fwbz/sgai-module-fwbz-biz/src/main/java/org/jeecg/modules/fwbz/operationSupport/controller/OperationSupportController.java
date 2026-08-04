package org.jeecg.modules.fwbz.operationSupport.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.dto.DeviceDataFindDto;
import org.jeecg.modules.fwbz.operationSupport.service.IOperationSupportService;
import org.jeecg.modules.fwbz.vo.DeviceDataVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fwbz/operationSupport")
@AllArgsConstructor
@Api(tags="运行保障")
@Slf4j
public class OperationSupportController {

    private final IOperationSupportService service;


//    /**
//     * 状态统计
//     * @return 统计结果
//     */
//    @GetMapping("/statistics")
//    public Result<?> deviceRunStateStatistics(){
//        return Result.ok(service.statistics());
//    }



    @GetMapping("/equipmentList")
    public Result<IPage<DeviceDataVo>> equipmentList(DeviceDataFindDto params) {
        return Result.ok(service.equipmentList(params));
    }

    /**
     * 空调机组
     * @param params
     * @return
     */
    @GetMapping("/airConditioningUnitList")
    public Result<IPage<DeviceDataVo>> airConditioningUnitList(DeviceDataFindDto params) {
        return Result.ok(service.airConditioningUnitList(params));
    }

}
