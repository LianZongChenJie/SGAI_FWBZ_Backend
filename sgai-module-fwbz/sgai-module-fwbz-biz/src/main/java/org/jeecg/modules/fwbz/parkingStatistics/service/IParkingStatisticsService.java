package org.jeecg.modules.fwbz.parkingStatistics.service;

import org.jeecg.modules.fwbz.parkingStatistics.vo.ParkingFlow24hVO;
import org.jeecg.modules.fwbz.parkingStatistics.vo.ParkingSpaceStatVO;
import org.jeecg.modules.fwbz.parkingStatistics.vo.ParkingStatCardVO;

import java.util.List;

/**
 * 停车统计Service接口
 */
public interface IParkingStatisticsService {

    /**
     * 今日进场车辆（同步外部数据 → 写入DB → 返回前端VO，含较昨日对比）
     */
    ParkingStatCardVO todayEntryCount();

    /**
     * 当前在场车辆（同步外部数据 → 写入DB → 返回前端VO，含较昨日对比）
     */
    ParkingStatCardVO currentInCount();

    /**
     * 剩余车位（同步外部数据 → 写入DB → 返回前端VO）
     */
    ParkingStatCardVO remainingSpaceCount();

    /**
     * 平均停车时长（同步外部数据 → 写入DB → 返回前端VO，含较昨日对比）
     */
    ParkingStatCardVO averageParkingDuration();

    /**
     * 汇总（同步四项 → 写入DB → 从DB读取返回全部卡片）
     */
    List<ParkingStatCardVO> getSummary();

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
