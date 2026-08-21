package org.jeecg.modules.fwbz.buildingControl.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 前端传入的实时数据更新项
 */
@Data
@ApiModel("实时数据更新项")
public class UpdRealDataItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 点ID */
    @NotNull(message = "tagid不能为空")
    @ApiModelProperty(value = "点ID", required = true, example = "223517")
    private Long tagid;

    /** 设定值（与修改点数据类型对应：BOOL/NUMBER/STRING） */
    @NotNull(message = "设定值pv不能为空")
    @ApiModelProperty(value = "设定值", required = true, example = "2489")
    private Object pv;

    /** 时间，可选，默认当前时间 */
    @ApiModelProperty(value = "时间，可选，默认当前时间", example = "2022-05-09 10:30:00.16")
    private String tm;

    /** 质量戳，可选，默认192 */
    @ApiModelProperty(value = "质量戳，可选，默认192", example = "192")
    private Integer qy;
}
