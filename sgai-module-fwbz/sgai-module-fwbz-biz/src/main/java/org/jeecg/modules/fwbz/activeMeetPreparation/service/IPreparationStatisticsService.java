package org.jeecg.modules.fwbz.activeMeetPreparation.service;

import org.jeecg.modules.fwbz.activeMeetStatistics.vo.StatCardVO;

import java.util.List;

public interface IPreparationStatisticsService {

    /**
     * 待筹备会展（明天以后的活动数）
     */
    StatCardVO pendingCount();

    /**
     * 筹备完成率（待筹备会展的平均进度）
     */
    StatCardVO completionRate();

    /**
     * 明日开展（明天开始的活动数）
     */
    StatCardVO tomorrowCount();

    /**
     * 会展检查项（设备类型总量）
     */
    StatCardVO checkItemCount();

    /**
     * 汇总统计（返回全部卡片）
     */
    List<StatCardVO> getSummary();
}
