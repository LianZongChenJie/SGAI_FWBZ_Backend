package org.jeecg.modules.fwbz.venueVisitorFlow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.sql.Time;
import java.time.LocalDate;

/**
 * 各场馆客流统计（每日每馆一条记录）
 * <p>对应达梦数据库表：FWBZ.table_venue_flow</p>
 *
 * <p>字段说明：
 * <ul>
 *     <li>dataDate       - 数据日期（一天一行）</li>
 *     <li>venueId        - 场馆id（来自 table_venue_info.id）</li>
 *     <li>todayInCount   - 今日进场人数</li>
 *     <li>todayNowCount  - 当前在场人数</li>
 *     <li>maxCount       - 峰值人数</li>
 *     <li>maxTime        - 峰值发生时间</li>
 *     <li>averageDuration - 平均停留时长（小时）</li>
 *     <li>status         - 状态（1=正常，0=异常）</li>
 * </ul>
 * </p>
 *
 * @author fwbz
 */
@Data
@TableName("table_venue_flow")
public class VenueFlow implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 数据日期（一天一行）
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataDate;

    /**
     * 场馆id
     */
    private Long venueId;

    /**
     * 今日进场人数
     */
    private Long todayInCount;

    /**
     * 当前在场人数
     */
    private Long todayNowCount;

    /**
     * 峰值人数
     */
    private Long maxCount;

    /**
     * 峰值时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "HH:mm:ss")
    @DateTimeFormat(pattern = "HH:mm:ss")
    private Time maxTime;

    /**
     * 平均停留时长（小时）
     */
    private Double averageDuration;

    /**
     * 状态（1=正常，0=异常）
     */
    private Integer status;
}