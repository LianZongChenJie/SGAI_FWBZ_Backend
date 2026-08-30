package org.jeecg.modules.fwbz.alarm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.fwbz.main.entity.BaseEntity;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 告警记录
 */
@EqualsAndHashCode(callSuper = true)
@TableName("alarm_record")
@Data
public class AlarmRecord extends BaseEntity {

    /**
     * 告警状态：未处理
     */
    public static final String ALARM_STATUS_UNTREATED = "1";

    /**
     * 告警状态：误报
     */
    public static final String ALARM_STATUS_TREATED = "2";
    /**
     * 告警状态：已转工单
     */
    public static final String ALARM_STATUS_EVENT = "3";
    /**
     * 告警状态：完成
     */
    public static final String ALARM_STATUS_COMPLETED = "4";

    /**
     * 告警规则id
     */
    private Long alarmRuleId;

    /**
     * 告警规则点位id
     */
    private Long alarmRulePointId;

    /**
     * 设备id
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    @Excel(name = "设备名称", width = 25)
    private String deviceName;

    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 空间名称
     */
    @Excel(name = "空间名称", width = 25)
    private String spaceName;

    /**
     * 设备类别id
     */
    private Long deviceCategoryId;

    /**
     * 告警内容
     */
    @Excel(name = "告警内容", width = 40)
    private String alarmContent;

    /**
     * 告警时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Excel(name = "告警时间", width = 22, format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime alarmTime;

    /**
     * 转工单时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transferEventTime;

    /**
     * 工单完成时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventCompletionTime;

    /**
     * 告警类别id
     */
    private Long alarmCategoryId;

    /**
     * 告警类别名称
     */
    @Excel(name = "告警类别", width = 20)
    private String alarmCategoryName;
    /**
     * 告警级别id
     */
    private Long alarmLevelId;
    /**
     * 告警级别名称
     */
    @Excel(name = "告警级别", width = 20)
    private String alarmLevelName;

    /**
     * 点位id
     */
    private Long pointId;

    @Excel(name = "点位名称", width = 25)
    private String pointName;

    /**
     * 时间粒度
     */
    @Excel(name = "时间粒度", width = 15)
    private String timeGranularity;

    /**
     * 点位值（告警值）
     */
    @Excel(name = "告警值", width = 15)
    private String value;

    /**
     * 条件值（阈值）
     */
    @Excel(name = "阈值", width = 15)
    private String conditionValue;

    /**
     * 条件
     */
    @Excel(name = "条件", width = 10)
    private String operator;

    /**
     * 负责人
     */
    private Long chargePerson;

    /**
     * 负责人
     */
    @Excel(name = "负责人", width = 15)
    private String chargePersonName;

    /**
     * 状态。未处理：1；已消除：2
     */
    @Excel(name = "告警状态", width = 12, replace = {"未处理_1", "已消除_2", "已转工单_3", "已完成_4"})
    private String alarmStatus;

    /**
     * 告警级别颜色
     */
    private String alarmLevelColor;

    /**
     * 事件id
     */
    private String eventId;
}
