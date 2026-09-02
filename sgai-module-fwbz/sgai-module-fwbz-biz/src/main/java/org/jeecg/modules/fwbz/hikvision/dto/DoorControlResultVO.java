package org.jeecg.modules.fwbz.hikvision.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 海康反向控制门禁点结果VO，返回前端逐项控制结果
 *
 * @author fwbz
 */
@Data
public class DoorControlResultVO {

    @ApiModelProperty(value = "门禁点唯一标识")
    private String doorIndexCode;

    @ApiModelProperty(value = "反控是否成功")
    private Boolean success;

    @ApiModelProperty(value = "反控结果码，0标识反控成功，其他表示失败")
    private Integer controlResultCode;

    @ApiModelProperty(value = "反控结果描述")
    private String controlResultDesc;
}
