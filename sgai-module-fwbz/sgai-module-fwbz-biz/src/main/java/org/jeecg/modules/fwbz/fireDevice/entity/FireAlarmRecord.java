package org.jeecg.modules.fwbz.fireDevice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 消防设备报警记录
 *
 * @author fwbz
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "消防设备报警记录", description = "消防设备报警记录")
@TableName("table_fire_alarm_record")
public class FireAlarmRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("消防设备ID")
    private String deviceId;

    @ApiModelProperty("报警日期")
    private LocalDate alarmDate;

    @ApiModelProperty("报警时间")
    private Time alarmTime;

    @ApiModelProperty("报警类型: 烟感报警/温感报警/手报报警/设备故障/低电量/离线")
    private String alarmType;

    @ApiModelProperty("报警级别: 1低 2中 3高 4紧急")
    private Integer alarmLevel;

    @ApiModelProperty("报警内容描述")
    private String alarmContent;

    @ApiModelProperty("报警位置")
    private String alarmLocation;

    @ApiModelProperty("处理状态: 0未处理 1处理中 2已处理 3误报 4忽略")
    private Integer handleStatus;

    @ApiModelProperty("处理人")
    private String handler;

    @ApiModelProperty("处理时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime handleTime;

    @ApiModelProperty("处理备注")
    private String handleRemark;

    @ApiModelProperty("状态: 1正常 0删除")
    private Integer status;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtCreate;

    @ApiModelProperty("修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtModified;
}
