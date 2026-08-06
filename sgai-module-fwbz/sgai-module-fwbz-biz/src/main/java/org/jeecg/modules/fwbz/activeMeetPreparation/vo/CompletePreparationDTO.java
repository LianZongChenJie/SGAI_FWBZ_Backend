package org.jeecg.modules.fwbz.activeMeetPreparation.vo;

import lombok.Data;

/**
 * 完成筹备项请求参数
 */
@Data
public class CompletePreparationDTO {

    private Long preparationInfoId;
    private Long preparationValue;
    private Long realValue;
}
