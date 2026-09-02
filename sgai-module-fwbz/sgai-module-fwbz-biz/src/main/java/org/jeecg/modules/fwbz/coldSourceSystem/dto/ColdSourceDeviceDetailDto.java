package org.jeecg.modules.fwbz.coldSourceSystem.dto;

import lombok.Data;
import org.jeecg.modules.fwbz.coldSourceSystem.entity.ColdSourceDeviceAttribute;

import java.util.List;

/**
 * 冷源设备详情返回（设备信息 + 属性列表）
 */
@Data
public class ColdSourceDeviceDetailDto {

    /** 主键 */
    private Long id;

    /** 设备编号 */
    private String deviceCode;

    /** 设备名称 */
    private String deviceName;

    /** 设备类别id */
    private Long categoryId;

    /** 设备类别名称 */
    private String categoryName;

    /** 所属系统 */
    private String systemCode;

    /** Niagara 路径 */
    private String niagaraPath;

    /** 状态: 1启用 0停用 */
    private Integer status;

    /** 排序 */
    private Integer sort;

    /** 备注 */
    private String remark;

    /** 关联属性列表 */
    private List<ColdSourceDeviceAttribute> attributes;
}
