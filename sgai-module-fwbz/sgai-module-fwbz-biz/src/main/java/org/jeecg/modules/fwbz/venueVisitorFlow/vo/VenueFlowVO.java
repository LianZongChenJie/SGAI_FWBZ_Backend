package org.jeecg.modules.fwbz.venueVisitorFlow.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.sql.Time;

/**
 * 各场馆客流统计 VO（前端表格展示 + Excel 导出）
 * <p>对应前端"各场馆客流统计"表格：场馆 / 今日进场 / 当前在场 / 峰值人数 / 峰值时间 / 平均停留 / 较昨日 / 状态</p>
 *
 * @author fwbz
 */
@Data
public class VenueFlowVO {

    /**
     * 场馆id
     */
    private Long venueId;

    /**
     * 场馆名称
     */
    @Excel(name = "场馆名称", width = 15)
    private String venueName;

    /**
     * 今日进场人数
     */
    @Excel(name = "今日进场人数", width = 15)
    private Long todayInCount;

    /**
     * 昨日进场人数
     */
    private Long yesterdayInCount;

    /**
     * 当前在场人数
     */
    @Excel(name = "当前在场人数", width = 15)
    private Long todayNowCount;

    /**
     * 昨日当前在场人数
     */
    private Long yesterdayNowCount;

    /**
     * 峰值人数
     */
    @Excel(name = "峰值人数", width = 15)
    private Long maxCount;

    /**
     * 峰值时间（HH:mm）
     */
    @JsonFormat(timezone = "GMT+8", pattern = "HH:mm")
    @Excel(name = "峰值时间", width = 15, format = "HH:mm")
    private Time maxTime;

    /**
     * 平均停留时长
     */
    @Excel(name = "平均停留时长", width = 15)
    private Double averageDuration;

    /**
     * 较昨日增减率描述（如 ↑18.5%）
     */
    @Excel(name = "较昨日", width = 15)
    private String compareRate;

    /**
     * 状态描述（如 正常 / 异常）
     */
    @Excel(name = "状态", width = 15)
    private String statusLabel;

    /**
     * 状态码（1=正常，0=异常）
     */
    private Integer status;
}