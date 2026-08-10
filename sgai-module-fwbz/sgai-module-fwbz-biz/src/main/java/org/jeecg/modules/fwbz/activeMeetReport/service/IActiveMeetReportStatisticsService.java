package org.jeecg.modules.fwbz.activeMeetReport.service;

import org.jeecg.modules.fwbz.activeMeetReport.vo.StatCardVO;

import java.util.List;

/**
 * 活动报告统计服务
 */
public interface IActiveMeetReportStatisticsService {

    /**
     * 待总结展会数
     */
    StatCardVO countPendingSummary();

    /**
     * 已总结展会数（含较上月对比）
     */
    StatCardVO countSummarized();

    /**
     * 报告生成
     */
    StatCardVO reportGeneration();

    /**
     * 知识库积累
     */
    StatCardVO knowledgeBaseAccumulation();

    /**
     * 汇总统计（返回全部卡片）
     */
    List<StatCardVO> getSummary();
}
