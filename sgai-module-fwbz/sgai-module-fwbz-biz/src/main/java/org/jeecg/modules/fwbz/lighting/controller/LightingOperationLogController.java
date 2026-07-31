package org.jeecg.modules.fwbz.lighting.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.lighting.dto.LightingOperationLogQueryDto;
import org.jeecg.modules.fwbz.lighting.service.ILightingOperationLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 照明控制记录
 */
@RestController
@RequestMapping("/Fwbz/lighting/operationLog")
@AllArgsConstructor
public class LightingOperationLogController {

    private final ILightingOperationLogService service;

    @GetMapping("/listPage")
    public Result<?> listPage(LightingOperationLogQueryDto param){
        return Result.ok(service.listPage(param));
    }
}
