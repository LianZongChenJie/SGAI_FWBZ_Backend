package org.jeecg.modules.fwbz.dataCollection.controller;

import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.dataCollection.service.IDataCollectionService;
import org.jeecg.modules.fwbz.dataCollection.vo.InterfaceListVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据采集 - 系统接口列表
 */
@RestController
@RequestMapping("/fwbz/dataCollection")
@AllArgsConstructor
public class DataCollectionController {

    private final IDataCollectionService dataCollectionService;

    /**
     * 获取系统接口列表（含采集量、完整率、最后采集时间）
     * @return
     */
    @GetMapping("/interfaceList")
    @AutoLog(value = "数据采集-系统接口列表")
    public Result<List<InterfaceListVO>> getInterfaceList() {
        return Result.ok(dataCollectionService.getInterfaceList());
    }
}
