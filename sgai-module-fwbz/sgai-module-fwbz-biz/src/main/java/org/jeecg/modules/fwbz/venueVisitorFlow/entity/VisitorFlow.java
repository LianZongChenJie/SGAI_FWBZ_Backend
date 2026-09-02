package org.jeecg.modules.fwbz.venueVisitorFlow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 场馆客流统计（每日一条记录）
 * <p>对应达梦数据库表：FWBZ.table_visitor_flow</p>
 *
 * @author fwbz
 */
@Data
@TableName("table_visitor_flow")
public class VisitorFlow implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 日期（一天一行）
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    /**
     * 今日总客流
     */
    private Long todayCount;

    /**
     * 当前在场客流
     */
    private Long nowCount;

    /**
     * 峰值客流
     */
    private Long maxCount;

    /**
     * 平均停留时长（小时）
     */
    private Double averageStopDuration;
}
