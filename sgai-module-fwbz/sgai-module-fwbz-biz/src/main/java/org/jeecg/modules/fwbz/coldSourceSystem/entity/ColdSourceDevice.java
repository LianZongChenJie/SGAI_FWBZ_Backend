package org.jeecg.modules.fwbz.coldSourceSystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 冷源设备信息表（FWBZ.cold_source_device）
 */
@Data
@TableName("\"FWBZ\".\"cold_source_device\"")
public class ColdSourceDevice {

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 设备编码(唯一) */
    private String deviceCode;

    /** 设备名称 */
    private String deviceName;

    /** 关联 cold_source_equipment_category.id */
    private Long categoryId;

    /** 所属系统 */
    private String systemCode;

    /** Niagara 路径 */
    private String niagaraPath;

    /** 状态: 1在线/启用 0离线/停用（属性采集定时任务更新在线状态） */
    private Integer status;

    /** 排序 */
    private Integer sort;

    /** 备注 */
    private String remark;

    /** 最后采集时间(属性采集定时任务更新) */
    private LocalDateTime lastTime;

    /** 创建人 */
    private String createBy;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新人 */
    private String updateBy;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 所属部门编码 */
    private String sysOrgCode;
}
