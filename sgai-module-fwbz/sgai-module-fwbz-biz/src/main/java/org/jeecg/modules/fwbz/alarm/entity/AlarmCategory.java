package org.jeecg.modules.fwbz.alarm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.fwbz.main.entity.BaseEntity;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * 告警类别
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("alarm_category")
public class AlarmCategory extends BaseEntity {

    /**
     * 状态。启用
     */
    public static final String STATUS_ENABLE = "1";
    /**
     * 状态。禁用
     */
    public static final String STATUS_DISABLE = "0";

    /**
     * 告警类别名称
     */
    @Excel(name = "告警类别名称", width = 25)
    private String alarmCategoryName;

    /**
     * 告警类别编码
     */
    @Excel(name = "告警类别编码", width = 20)
    private String alarmCategoryCode;

    /**
     * 排序
     */
    @Excel(name = "排序", width = 10)
    private Integer sort;

    /**
     * 状态。启用：1；禁用：0
     */
    @Excel(name = "状态", width = 12, replace = {"启用_1", "禁用_0"})
    private String status;
}
