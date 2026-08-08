package org.jeecg.modules.fwbz.hikvision.service;

import org.jeecg.modules.fwbz.hikvision.dto.StatCardVO;

import java.util.List;

/**
 * 门禁统计服务接口
 *
 * @author fwbz
 */
public interface IDoorStatisticsService {

    /**
     * 总门禁点位数量
     */
    StatCardVO countTotalDoorPoints();

    /**
     * 在线门禁点位数量
     */
    StatCardVO countOnlineDoorPoints();

    /**
     * 门禁设备总数
     */
    StatCardVO countTotalDevices();

    /**
     * 在线门禁设备数
     */
    StatCardVO countOnlineDevices();

    /**
     * 门禁点当天事件总数
     */
    StatCardVO countTodayDoorEvents();

    /**
     * 汇总统计（返回全部卡片）
     */
    List<StatCardVO> getSummary();
}
