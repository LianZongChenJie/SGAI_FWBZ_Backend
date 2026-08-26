package org.jeecg.modules.fwbz.coldSourceSystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 冷源对应关系表（FWBZ.table_tagid_info）
 * 维护采集点ID(tag_id) 与描述、类型、前端数据、是否存储(is_save) 的映射，
 * is_save 为 '1' 表示该采集点需要定时存储历史数据。
 */
@Data
@TableName("\"FWBZ\".\"table_tagid_info\"")
public class TableTagidInfo {

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 采集点id */
    private Long tagId;

    /** 描述 */
    @TableField("\"desc\"")
    private String desc;

    /** 类型 */
    private String type;

    /** 对应前端数据 */
    private String frontData;

    /** 是否存储（1=存储） */
    private String isSave;
}
