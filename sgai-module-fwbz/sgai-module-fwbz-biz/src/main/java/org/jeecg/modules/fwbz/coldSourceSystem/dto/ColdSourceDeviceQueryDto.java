package org.jeecg.modules.fwbz.coldSourceSystem.dto;

import lombok.Data;

/**
 * 冷源设备列表查询入参
 */
@Data
public class ColdSourceDeviceQueryDto {

    /** 设备名称（模糊匹配） */
    private String deviceName;

    /** 设备编号（模糊匹配） */
    private String deviceCode;

    /** 设备状态（精确匹配，1启用 0停用） */
    private Integer status;

    /** 设备类别id（精确匹配，关联 cold_source_equipment_category.id） */
    private Long categoryId;

    /** 页码（可不传，不传时按业务场景兜底，如导出全部） */
    private Integer pageNo;

    /** 每页条数（可不传） */
    private Integer pageSize;
}
