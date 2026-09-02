package org.jeecg.modules.fwbz.activeMeetPreparation.vo;

import lombok.Data;

import java.util.List;

/**
 * 按筹备类型分组
 */
@Data
public class DeviceTypeGroupVO {

    private Long typeId;
    private String typeName;
    private String preparationProgress;
    private List<PreparationDetailVO> typeData;
}
