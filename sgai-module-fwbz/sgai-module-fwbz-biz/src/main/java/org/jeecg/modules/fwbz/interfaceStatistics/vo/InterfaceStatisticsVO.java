package org.jeecg.modules.fwbz.interfaceStatistics.vo;

import lombok.Data;

/**
 * 接口统计看板数据
 */
@Data
public class InterfaceStatisticsVO {

    /**
     * 对接系统数
     */
    private Long connectedSystemCount;

    /**
     * 接口在线率，如 99.2%
     */
    private String onlineRate;

    /**
     * 今日数据量，如 2.8M
     */
    private String todayDataSize;

    /**
     * 异常接口数
     */
    private Long abnormalCount;
}
