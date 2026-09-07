package org.jeecg.modules.fwbz.buildingControl.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 更新设备属性数据类型接口响应
 */
@Data
@ApiModel(value = "UpdateValueTypeResponseDto", description = "更新设备属性数据类型结果")
public class UpdateValueTypeResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "需处理属性总数（采集编码为数字）")
    private int total;

    @ApiModelProperty(value = "更新成功数")
    private int success;

    @ApiModelProperty(value = "更新失败数")
    private int failed;

    @ApiModelProperty(value = "失败明细")
    private List<String> failDetails = new ArrayList<>();
}
