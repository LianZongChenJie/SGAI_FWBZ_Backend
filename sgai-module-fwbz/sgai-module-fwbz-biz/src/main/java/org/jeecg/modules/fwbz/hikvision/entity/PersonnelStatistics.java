package org.jeecg.modules.fwbz.hikvision.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 人员统计表
 *
 * @author fwbz
 */
@Data
@TableName("table_personnel_statistics")
public class PersonnelStatistics implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 统计日期 */
    @TableField("stat_date")
    private LocalDate statDate;

    /** 今日进场人数 */
    @TableField("today_entry_count")
    private Long todayEntryCount;

    /** 当前在场人数 */
    @TableField("current_in_count")
    private Long currentInCount;

    /** 人员识别记录数 */
    @TableField("recognition_record_count")
    private Long recognitionRecordCount;

    /** 异常行为预警数 */
    @TableField("abnormal_warning_count")
    private Long abnormalWarningCount;

    /** 记录创建时间 */
    @TableField("gmt_create")
    private LocalDateTime gmtCreate;

    /** 记录更新时间 */
    @TableField("gmt_modified")
    private LocalDateTime gmtModified;
}
