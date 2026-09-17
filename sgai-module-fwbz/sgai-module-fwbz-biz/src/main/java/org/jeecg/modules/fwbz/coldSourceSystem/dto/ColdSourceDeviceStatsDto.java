package org.jeecg.modules.fwbz.coldSourceSystem.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 冷源设备统计 DTO（首页统计卡片）
 */
@Data
public class ColdSourceDeviceStatsDto {

    /** 冷源机组总数 */
    private Long deviceTotal;

    /** 在线数量(status=1) */
    private Long onlineCount;

    /** 离线数量(status=0) */
    private Long offlineCount;

    /** 今日制冷量（tagid 542/549/556/563/577 今日最新值 - 今日0点值 之和） */
    private BigDecimal todayCoolingCapacity;

    /** 今日总功率（tagid 543/550/557/564/578 今日最新值之和） */
    private BigDecimal todayPower;

    /** 平均COP（今日制冷量 / 今日总功率，功率为0或无数据时为null） */
    private BigDecimal avgCop;
}
