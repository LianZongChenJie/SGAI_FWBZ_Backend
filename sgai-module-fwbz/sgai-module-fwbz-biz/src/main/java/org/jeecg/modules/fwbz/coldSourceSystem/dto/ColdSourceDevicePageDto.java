package org.jeecg.modules.fwbz.coldSourceSystem.dto;

import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * 冷源设备列表返回（关联 cold_source_equipment_category 取类别名称）
 */
@Data
public class ColdSourceDevicePageDto {

    /** 主键 */
    private Long id;

    /** 设备编号 */
    @Excel(name = "设备编号", width = 20)
    private String deviceCode;

    /** 设备名称 */
    @Excel(name = "设备名称", width = 25)
    private String deviceName;

    /** 设备类别id */
    private Long categoryId;

    /** 设备类别名称 */
    @Excel(name = "设备类别", width = 20)
    private String categoryName;

    /** 所属系统 */
    @Excel(name = "所属系统", width = 20)
    private String systemCode;

    /** Niagara 路径 */
    @Excel(name = "Niagara路径", width = 25)
    private String niagaraPath;

    /** 状态: 1启用 0停用 */
    @Excel(name = "状态", width = 10, replace = {"启用_1", "停用_0"})
    private Integer status;

    /** 排序 */
    private Integer sort;

    /** 备注 */
    @Excel(name = "备注", width = 30)
    private String remark;
}
