package org.jeecg.modules.fwbz.securityStatistics.service;

import org.jeecg.modules.fwbz.securityStatistics.vo.SecurityStatCardVO;

import java.util.List;

/**
 * 安防统计服务
 */
public interface ISecurityStatisticsService {

    /**
     * 监控摄像头总数（含今日新增数量）
     */
    SecurityStatCardVO cameraTotal();

    /**
     * 在线摄像头数量（含在线率）
     */
    SecurityStatCardVO cameraOnline();

    /**
     * 今日视频巡更完成情况
     */
    SecurityStatCardVO patrolPlanToday();

    /**
     * AI事件分析数量（较昨日对比）
     */
    SecurityStatCardVO aiEventAnalysis();

    /**
     * 汇总统计（返回全部卡片）
     */
    List<SecurityStatCardVO> getSummary();
}
