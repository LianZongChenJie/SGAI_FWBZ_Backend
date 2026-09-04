package org.jeecg.modules.fwbz.centralizedAirCooling.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 集中风冷系统总览数据 DTO
 */
@Data
@ApiModel(value = "集中风冷系统总览数据", description = "风冷系统总功率/当前制冷量/风冷系统COP/今日累计用电量")
public class CentralizedAirCoolingOverviewDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "风冷系统总功率(kW)")
    private BigDecimal totalPower;

    @ApiModelProperty(value = "当前制冷量(kW)")
    private BigDecimal currentCoolingCapacity;

    @ApiModelProperty(value = "风冷系统COP")
    private BigDecimal cop;

    @ApiModelProperty(value = "今日累计用电量(kWh)")
    private BigDecimal todayPowerConsumption;
}
