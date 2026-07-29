package org.jeecg.modules.fwbz.activeMeet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
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
    private Date startDate;

    /**
     * 开始时间
     */
    @DateTimeFormat(pattern = "HH:mm:ss")
    private Time startTime;

    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "HH:mm:ss")
    private Time endTime;

    /**
     * 预计人数
     */
    private Long peopleQuantity;
}
