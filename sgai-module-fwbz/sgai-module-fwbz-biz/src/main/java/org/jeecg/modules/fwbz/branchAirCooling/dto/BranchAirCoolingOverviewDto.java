package org.jeecg.modules.fwbz.branchAirCooling.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 分馆风冷系统总览数据 DTO
 */
@Data
@ApiModel(value = "分馆风冷系统总览数据", description = "分馆风冷总功率/当前制冷量/分馆综合COP/今日累计用电量")
public class BranchAirCoolingOverviewDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "分馆风冷总功率(kW)")
    private BigDecimal totalPower;

    @ApiModelProperty(value = "当前制冷量(kW)")
    private BigDecimal currentCoolingCapacity;

    @ApiModelProperty(value = "分馆综合COP")
    private BigDecimal cop;

    @ApiModelProperty(value = "今日累计用电量(kWh)")
    private BigDecimal todayPowerConsumption;
}
