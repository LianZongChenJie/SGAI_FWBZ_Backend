package org.jeecg.modules.fwbz.parkingStatistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 停车统计
 */
@Data
@TableName("table_parking_count")
public class ParkingCount {

    /**
     * 主键
     */
    private Long id;

    /**
     * 日期
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    /**
     * 今日进场车辆数
     */
    private Long todayEntryCount;

    /**
     * 当前在场车辆数
     */
    private Long currentInCount;

    /**
     * 剩余车位数
     */
    private Long remainingSpaceCount;

    /**
     * 平均停车时长（小时）
     */
    private Double averageParkingDuration;
}
