package org.jeecg.modules.fwbz.complaint.service;

import org.jeecg.modules.fwbz.activeMeetStatistics.vo.StatCardVO;

import java.util.List;

/**
 * @Description: 首页统计卡片
 * @Author: jeecg-boot
 * @Date:   2026-08-06
 * @Version: V1.0
 */
public interface IComplaintStatisticsService {

    /**
     * 今日会展卡片
     */
    StatCardVO todayActiveMeet();

    /**
     * 调度指令卡片
     */
    StatCardVO todayDispatchOrder();

    /**
     * 投诉建议卡片
     */
    StatCardVO todayComplaint();

    /**
     * 设备异常卡片
     */
    StatCardVO todayAlarm();

    /**
     * 汇总统计（返回全部卡片）
     */
    List<StatCardVO> getSummary();
}
