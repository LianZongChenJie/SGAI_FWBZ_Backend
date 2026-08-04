package org.jeecg.modules.fwbz.parkingStatistics.service;

import org.jeecg.modules.fwbz.parkingStatistics.vo.ParkingFlow24hVO;
import org.jeecg.modules.fwbz.parkingStatistics.vo.ParkingSpaceStatVO;
import org.jeecg.modules.fwbz.parkingStatistics.vo.ParkingStatCardVO;

import java.util.List;

/**
 * 停车统计Service接口
 */
public interface IParkingStatisticsService {

    // ==================== 数据同步（由定时任务调用） ====================

    /**
     * 同步全部四项停车数据到数据库（今日进场/当前在场/剩余车位/平均停车时长）。
     * <p>由定时任务 ParkingStatisticsJob 每5分钟自动调用。</p>
     */
    void syncAllFromApi();

    // ==================== 卡片查询（仅读库，同步由定时任务负责） ====================

    /**
     * 从数据库读取今日进场车辆（含较昨日对比）
     */
    ParkingStatCardVO queryTodayEntryCount();

    /**
     * 从数据库读取当前在场车辆（含较昨日对比）
     */
    ParkingStatCardVO queryCurrentInCount();

    /**
     * 从数据库读取剩余车位
     */
    ParkingStatCardVO queryRemainingSpaceCount();

    /**
     * 从数据库读取平均停车时长（含较昨日对比）
     */
    ParkingStatCardVO queryAverageParkingDuration();

    /**
     * 从数据库读取全部四张停车卡片
     */
    List<ParkingStatCardVO> querySummary();

    // ==================== 实时数据（直接从外部系统获取，不落库） ====================

    /**
     * 停车场实时车位分布（直接从外部系统获取，不落库）
     * <p>
     * 用于"停车场实时状态"图，示例：P1: 156/300 | P2: 89/200 ...
     */
    List<ParkingSpaceStatVO> getParkingSpaceDistribution();

    /**
     * 24 小时停车流量趋势（直接从外部系统获取，不落库）
     * <p>
     * 保持外部API原始返回格式
     */
    ParkingFlow24hVO getParkingFlow24h();
}
