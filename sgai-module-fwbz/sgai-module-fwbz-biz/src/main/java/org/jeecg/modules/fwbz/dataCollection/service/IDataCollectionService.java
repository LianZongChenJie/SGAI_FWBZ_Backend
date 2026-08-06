package org.jeecg.modules.fwbz.dataCollection.service;

import org.jeecg.modules.fwbz.activeMeetStatistics.vo.StatCardVO;
import org.jeecg.modules.fwbz.dataCollection.vo.InterfaceListVO;

import java.util.List;

public interface IDataCollectionService {

    /**
     * 获取系统接口列表（含采集量、完整率、最后采集时间）
     */
    List<InterfaceListVO> getInterfaceList();

    /**
     * 采集点位数：所有系统采集点位之和
     */
    StatCardVO collectionPointCount();

    /**
     * 今日采集量：table_interface_history 取今日之和
     */
    StatCardVO todayCollectionAmount();

    /**
     * 数据完整率：所有系统的平均
     */
    StatCardVO dataCompletenessRate();

    /**
     * 存储容量：table_interface_history 所有采集量之和
     */
    StatCardVO storageCapacity();

    /**
     * 汇总统计（返回全部四张卡片）
     */
    List<StatCardVO> getSummary();
}
