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
 * 各场馆客流分时统计
 * <p>对应达梦数据库表：FWBZ.table_venue_flow_hour</p>
 *
 * @author fwbz
 */
@Data
@TableName("table_venue_flow_hour")
public class VenueFlowHour implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 数据日期 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataDate;

    /** 场馆id */
    private Long venueId;

    /** 进场人数（该小时累计） */
    private Long todayInCount;

    /** 在场人数 */
    private Long todayNowCount;

    /** 峰值人数 */
    private Long maxCount;

    /** 峰值时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "HH:mm:ss")
    @DateTimeFormat(pattern = "HH:mm:ss")
    private Time maxTime;

    /** 平均停留时长（小时） */
    private Double averageDuration;

    /** 状态 */
    private Integer status;

    /** 时间（小时） */
    @JsonFormat(timezone = "GMT+8", pattern = "HH:mm:ss")
    @DateTimeFormat(pattern = "HH:mm:ss")
    private Time dataHour;
}
