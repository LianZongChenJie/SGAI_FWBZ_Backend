package org.jeecg.modules.fwbz.fireDevice.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 按设备类型统计状态数量 VO
 *
 * @author fwbz
 */
@Data
@ApiModel(value = "按设备类型统计状态数量", description = "按设备类型分组统计各状态设备数量")
public class DeviceTypeStatusVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("设备类型名称")
    private String typeName;

    @ApiModelProperty("该类型下各状态统计列表")
    private List<StatusCountVO> data;
}
