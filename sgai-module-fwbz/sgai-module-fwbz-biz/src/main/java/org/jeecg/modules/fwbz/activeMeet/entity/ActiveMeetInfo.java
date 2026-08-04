package org.jeecg.modules.fwbz.activeMeet.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.fwbz.entity.BaseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Time;
import java.util.Date;

/**
 * 活动信息
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("table_activeMeet_info")
public class ActiveMeetInfo extends BaseEntity {

    /**
     * 活动名称
     */
    private String activeName;

    /**
     * 场馆id
     */
    private Long venueId;

    /**
     * 活动层数
     */
    private String venueFloors;

    /**
     * 开始日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date startDate;

    /**
     * 开始时间
     */
    @DateTimeFormat(pattern = "HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "HH:mm:ss")
    private Time startTime;

    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "HH:mm:ss")
    private Time endTime;

    /**
     * 预计人数
     */
    private Long peopleQuantity;

    /**
     * 场馆名称（非数据库字段）
     */
    @TableField(exist = false)
    private String venueName;
}
