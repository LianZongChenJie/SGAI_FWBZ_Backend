package org.jeecg.modules.fwbz.coldSourceSystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 页面与冷源对应关系表（FWBZ.table_page_info）
 * 维护前端字段 key(front_data) 与采集点ID(tag_id) 的映射，
 * tag_id 为 NULL 表示该 key 在点表中无对应测点（前端可兜底）。
 */
@Data
@TableName("\"FWBZ\".\"table_page_info\"")
public class TablePageInfo {

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 采集点id（测点ID，NULL 表示无对应测点） */
    private Long tagId;

    /** 描述（对应点表描述信息，仅作参考） */
    @TableField("\"desc\"")
    private String desc;

    /** 类型（psAnalog/psDigital） */
    private String type;

    /** 对应前端数据（前端字段 key） */
    private String frontData;

    /** 是否存储 */
    private String isSave;
}
