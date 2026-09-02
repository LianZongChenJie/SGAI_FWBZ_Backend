package org.jeecg.modules.fwbz.interfaceStatistics.service;

import org.jeecg.modules.fwbz.activeMeetStatistics.vo.StatCardVO;

import java.util.List;

public interface IInterfaceStatisticsService {

    /**
     * 对接系统数
     */
    StatCardVO connectedSystemCount();

    /**
     * 接口在线率
     */
    StatCardVO onlineRate();

    /**
     * 今日数据量（含较昨日对比）
     */
    StatCardVO todayDataSize();

    /**
     * 异常接口数
     */
    StatCardVO abnormalCount();

    /**
     * 汇总统计（返回全部卡片）
     */
    List<StatCardVO> getSummary();
}
