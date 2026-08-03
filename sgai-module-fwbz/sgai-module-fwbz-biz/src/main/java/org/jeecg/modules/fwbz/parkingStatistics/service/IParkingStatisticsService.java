package org.jeecg.modules.fwbz.parkingStatistics.service;

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
}
