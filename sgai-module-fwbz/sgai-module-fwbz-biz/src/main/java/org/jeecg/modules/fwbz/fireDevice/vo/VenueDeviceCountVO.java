package org.jeecg.modules.fwbz.fireDevice.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 场馆消防设备数量统计 VO
 *
 * @author fwbz
 */
@Data
@ApiModel(value = "场馆消防设备数量统计", description = "按场馆统计消防设备数量，联动返回场馆经纬度")
public class VenueDeviceCountVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("场馆名称")
    private String venueName;

    @ApiModelProperty("经度")
    private BigDecimal longitude;

    @ApiModelProperty("纬度")
    private BigDecimal latitude;

    @ApiModelProperty("消防设备数量")
    private Long deviceCount;
}
