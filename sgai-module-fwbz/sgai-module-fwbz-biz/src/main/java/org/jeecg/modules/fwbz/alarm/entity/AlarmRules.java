package org.jeecg.modules.fwbz.alarm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.fwbz.main.entity.BaseEntity;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.util.List;

/**
 * 告警规则
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("alarm_rules")
public class AlarmRules extends BaseEntity {

    /**
     * 瞬时值
     */
    public static final String POINT_TYPE_INSTANT = "instant";
    /**
     * 累计值
     */
    public static final String POINT_TYPE_ACCUMULATE = "accumulate";

    /**
     * 计量规则累计值
     */
    public static final String POINT_TYPE_VIRTUAL = "virtual";

    /**
     * 启用
     */
    public static final String ENABLED_STATUS_ENABLE = "1";
    /**
     * 禁用
     */
    public static final String ENABLED_STATUS_DISABLE = "0";

    /** 规则编号 */
    @Excel(name = "规则编号", width = 20)
    private String ruleCode;
    /** 规则名称 */
    @Excel(name = "规则名称", width = 25)
    private String ruleName;
    /** 报警类别 */
    private Long alarmCategoryId;
    /** 报警类别名称 */
    @Excel(name = "报警类别", width = 20)
    private String alarmCategoryName;
    /** 报警等级 */
    private Long alarmLevelId;
    /** 报警等级名称 */
    @Excel(name = "报警等级", width = 20)
    private String alarmLevelName;

    /**
     * 频率
     */
    @Excel(name = "频率", width = 10)
    private Integer frequency;

    /**
     * 频率单位。秒：s；分钟：m；小时：h；天：d
     */
    @Excel(name = "频率单位", width = 12, replace = {"秒_s", "分钟_m", "小时_h", "天_d"})
    private String frequencyUnit;

    /**
     * 报警点位类型
     */
    @Excel(name = "报警点位类型", width = 18, replace = {"瞬时值_instant", "累计值_accumulate", "计量规则累计值_virtual"})
    private String pointType;

    /** 通知用户id */
    @Excel(name = "通知用户", width = 20)
    private String noticeUser;

    /**
     * 启用状态，启用：1；禁用：0
     */
    @Excel(name = "启用状态", width = 12, replace = {"启用_1", "禁用_0"})
    private String enabledStatus;

    /**
     * 告警级别颜色
     */
    @Excel(name = "告警级别颜色", width = 15)
    private String alarmLevelColor;

    @TableField(exist = false)
    private List<AlarmRulePoint> points;
}
