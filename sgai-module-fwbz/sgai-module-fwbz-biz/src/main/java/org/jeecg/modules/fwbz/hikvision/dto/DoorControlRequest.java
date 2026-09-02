package org.jeecg.modules.fwbz.hikvision.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 海康反向控制门禁点请求参数（/api/acs/v1/door/doControl）
 * <p>控制门禁点开关，doorIndexCodes 最多支持10个门禁点。</p>
 *
 * @author fwbz
 */
@Data
public class DoorControlRequest {

    /** 门禁点唯一标识，最大支持10个门禁点 */
    @ApiModelProperty(value = "门禁点唯一标识，最大支持10个门禁点", required = true)
    private List<String> doorIndexCodes;

    /** 控制类型：0-常开，1-门闭，2-门开，3-常闭 */
    @ApiModelProperty(value = "控制类型：0-常开，1-门闭，2-门开，3-常闭", required = true)
    private Integer controlType;
}
