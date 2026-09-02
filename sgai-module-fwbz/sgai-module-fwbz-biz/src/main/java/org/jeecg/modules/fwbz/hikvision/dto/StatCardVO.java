package org.jeecg.modules.fwbz.hikvision.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 门禁统计卡片VO
 *
 * @author fwbz
 */
@Data
public class StatCardVO {

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "数值")
    private Number value;

    @ApiModelProperty(value = "上下文描述（如 在线率xx%、较昨日↑3）")
    private String context;
}
