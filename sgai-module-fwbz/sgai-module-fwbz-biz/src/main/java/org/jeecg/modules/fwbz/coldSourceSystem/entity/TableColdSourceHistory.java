package org.jeecg.modules.fwbz.coldSourceSystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 冷源系统存储数据表（FWBZ.table_cold_source_history）
 * 定时保存的冷源历史数据：tagid + 值 + 值类型 + 记录时间。
 */
@Data
@TableName("\"FWBZ\".\"table_cold_source_history\"")
public class TableColdSourceHistory {

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** tagid（采集点id） */
    private Long tagId;

    /** 值 */
    private String value;

    /** 值类型 */
    private String valueType;

    /** 记录时间 */
    private LocalDateTime dataTime;
}
