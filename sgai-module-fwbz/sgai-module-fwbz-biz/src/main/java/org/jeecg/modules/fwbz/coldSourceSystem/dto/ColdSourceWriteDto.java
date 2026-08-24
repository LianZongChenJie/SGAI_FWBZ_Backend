package org.jeecg.modules.fwbz.coldSourceSystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 冷源系统写点（更新点位信息数据）请求参数
 */
@Data
public class ColdSourceWriteDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 测点ID（单点写时使用，对应 device_attribute.acquisition_coding） */
    private Long tagId;

    /** 写入值（单点写） */
    private String value;

    /** 批量写：测点ID列表（与 values 一一对应） */
    private List<Long> tagIds;

    /** 批量写：值列表（与 tagIds 一一对应） */
    private List<String> values;
}
