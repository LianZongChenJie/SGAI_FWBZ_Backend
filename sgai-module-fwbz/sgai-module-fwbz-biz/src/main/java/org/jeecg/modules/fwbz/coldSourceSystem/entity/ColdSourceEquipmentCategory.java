package org.jeecg.modules.fwbz.coldSourceSystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 冷源设备类别表（FWBZ.cold_source_equipment_category）
 */
@Data
@TableName("\"FWBZ\".\"cold_source_equipment_category\"")
public class ColdSourceEquipmentCategory {

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 父级id */
    private Long pid;

    /** 是否有子节点: 1有 0无 */
    private Integer hasChild;

    /** 分类名称 */
    private String categoryName;

    /** 排序 */
    private Integer sort;

    /** 备注（含分类编码） */
    private String remark;

    /** 全名 */
    private String fullName;

    /** 全id */
    private String fullId;

    /** 类别类型: 1计量 2楼控 */
    private Integer type;

    /** 主id */
    private Long masterId;
}
