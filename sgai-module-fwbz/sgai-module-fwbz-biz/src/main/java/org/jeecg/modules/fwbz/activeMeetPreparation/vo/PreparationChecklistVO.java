package org.jeecg.modules.fwbz.activeMeetPreparation.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 会前筹备清单响应
 */
@Data
public class PreparationChecklistVO {

    private Long activeMeetId;
    private String activeName;
    private String preparationProgress;
    private List<DeviceTypeGroupVO> data;
}
