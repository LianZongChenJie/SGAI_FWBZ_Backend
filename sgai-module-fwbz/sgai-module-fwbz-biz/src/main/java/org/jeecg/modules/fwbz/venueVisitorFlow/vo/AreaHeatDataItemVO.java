package org.jeecg.modules.fwbz.venueVisitorFlow.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 区域热力图数据项
 *
 * @author fwbz
 */
@Data
@ApiModel(value = "区域热力图数据项", description = "区域热力图经纬度及人数")
public class AreaHeatDataItemVO {

    @ApiModelProperty("人数")
    private Integer count;

    @ApiModelProperty("纬度")
    private BigDecimal lat;

    @ApiModelProperty("经度")
    private BigDecimal lon;
}
