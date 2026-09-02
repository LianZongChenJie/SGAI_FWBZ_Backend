package org.jeecg.modules.fwbz.activeMeet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 会前筹备信息表
 */
@Data
@TableName("table_activeMeet_preparation_info")
public class ActiveMeetPreparationInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会议id
     */
    private Long activeMeetId;

    /**
     * 筹备设备id（关联table_activeMeets_device_type.id）
     */
    private Long activeMeetsDeviceTypeId;

    /**
     * 筹备数量
     */
    private Long preparationValue;

    /**
     * 在线数量
     */
    private Long realValue;

    /**
     * 状态，0：未完成，1已完成
     */
    private Integer status;

    /**
     * 完成时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date completeTime;
}
