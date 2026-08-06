package org.jeecg.modules.fwbz.dataCollection.controller;

import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.activeMeetStatistics.vo.StatCardVO;
import org.jeecg.modules.fwbz.dataCollection.service.IDataCollectionService;
import org.jeecg.modules.fwbz.dataCollection.vo.InterfaceListVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据采集 - 系统接口列表与统计
 */
@RestController
@RequestMapping("/fwbz/dataCollection")
@AllArgsConstructor
public class DataCollectionController {

    private final IDataCollectionService dataCollectionService;

    /**
     * 获取系统接口列表（含采集量、完整率、最后采集时间）
     */
    @GetMapping("/interfaceList")
    @AutoLog(value = "数据采集-系统接口列表")
    public Result<List<InterfaceListVO>> getInterfaceList() {
        return Result.ok(dataCollectionService.getInterfaceList());
    }

    /**
     * 采集点位数：所有系统采集点位之和
     */
    @GetMapping("/collectionPointCount")
    @AutoLog(value = "数据采集-采集点位数")
    public Result<StatCardVO> collectionPointCount() {
        return Result.ok(dataCollectionService.collectionPointCount());
    }

    /**
     * 今日采集量：table_interface_history 取今日之和
     */
    @GetMapping("/todayCollectionAmount")
    @AutoLog(value = "数据采集-今日采集量")
    public Result<StatCardVO> todayCollectionAmount() {
        return Result.ok(dataCollectionService.todayCollectionAmount());
    }

    /**
     * 数据完整率：所有系统的平均
     */
    @GetMapping("/dataCompletenessRate")
    @AutoLog(value = "数据采集-数据完整率")
    public Result<StatCardVO> dataCompletenessRate() {
        return Result.ok(dataCollectionService.dataCompletenessRate());
    }

    /**
     * 存储容量：table_interface_history 所有采集量之和
     */
    @GetMapping("/storageCapacity")
    @AutoLog(value = "数据采集-存储容量")
    public Result<StatCardVO> storageCapacity() {
        return Result.ok(dataCollectionService.storageCapacity());
    }

    /**
     * 汇总统计（返回全部四张卡片）
     */
    @GetMapping("/summary")
    @AutoLog(value = "数据采集-统计汇总")
    public Result<List<StatCardVO>> summary() {
        return Result.ok(dataCollectionService.getSummary());
    }
}
