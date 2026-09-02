package org.jeecg.modules.fwbz.activeMeetPreparation.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 筹备明细
 */
@Data
public class PreparationDetailVO {

    private Long preparationInfoId;
    private String preparationInfoName;
    private Long preparationValue;
    private Long realValue;
    private Integer status;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date completeTime;
}
