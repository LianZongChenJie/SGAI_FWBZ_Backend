package org.jeecg.modules.fwbz.parkingStatistics.vo;

import lombok.Data;

import java.util.List;

/**
 * 24小时停车流量VO（保持外部API原始返回格式）
 */
@Data
public class ParkingFlow24hVO {

    /** 小时标签数组 */
    private List<String> date;

    /** 进场车辆数数组 */
    private List<Long> in;

    /** 出场车辆数数组 */
    private List<Long> out;

    /** 进出汇总数数组 */
    private List<Long> total;

    /** 今日进场总数 */
    private Long todayInTotal;

    /** 今日出场总数 */
    private Long todayOutTotal;

    /** 今日进出汇总 */
    private Long todayInOutTotal;
}
