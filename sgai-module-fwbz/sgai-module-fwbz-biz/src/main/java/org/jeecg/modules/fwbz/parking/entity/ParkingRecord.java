package org.jeecg.modules.fwbz.parking.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.fwbz.entity.BaseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 停车记录
 */
@EqualsAndHashCode(callSuper = true)
@TableName("table_parking_record")
@Data
public class ParkingRecord extends BaseEntity {

    /**
     * 时间，格式 HH24:MI:SS
     */
    private String parkTime;

    /**
     * 日期
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate parkDate;

    /**
     * 车牌号
     */
    private String plateNo;

    /**
     * 类型，如进场、出场
     */
    private String parkType;

    /**
     * 停车场名称
     */
    private String parkingLot;

    /**
     * 方向，如入口、出口
     */
    private String direction;

    /**
     * 车位号
     */
    private String spaceNo;

    /**
     * 停车时长，如 2小时30分钟
     */
    private String parkDuration;

    /**
     * 记录创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gmtCreate;

    /**
     * 记录更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gmtModified;
}
