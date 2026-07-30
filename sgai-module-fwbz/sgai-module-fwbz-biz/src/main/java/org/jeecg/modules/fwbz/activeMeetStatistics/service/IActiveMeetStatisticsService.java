package org.jeecg.modules.fwbz.activeMeetStatistics.service;

import org.jeecg.modules.fwbz.activeMeetStatistics.vo.StatCardVO;

import java.util.List;

public interface IActiveMeetStatisticsService {

    /**
     * 本月活动数（含较上月对比）
     */
    StatCardVO countThisMonth();

    /**
     * 今日活动数
     */
    StatCardVO countToday();

    /**
     * 下周活动数（待筹备）
     */
    StatCardVO countNextWeek();

    /**
     * 场馆利用率（含较上月对比）
     */
    StatCardVO venueUtilization();

    /**
     * 汇总统计（返回全部卡片）
     */
    List<StatCardVO> getSummary();
}
