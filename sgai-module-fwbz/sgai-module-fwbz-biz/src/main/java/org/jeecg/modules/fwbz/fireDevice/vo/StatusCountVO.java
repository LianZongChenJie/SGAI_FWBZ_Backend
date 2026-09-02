package org.jeecg.modules.fwbz.fireDevice.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 设备状态统计 VO
 *
 * @author fwbz
 */
@Data
@ApiModel(value = "设备状态统计", description = "按状态统计设备数量")
public class StatusCountVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("状态名称")
    private String status;

    @ApiModelProperty("设备数量")
    private Long count;
}
