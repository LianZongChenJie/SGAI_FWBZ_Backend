package org.jeecg.modules.fwbz.activeMeetReport.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 展会总结报告
 * @Author: jeecg-boot
 * @Date:   2026-08-08
 * @Version: V1.0
 */
@Data
@TableName("table_activeMeet_report")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="table_activeMeet_report对象", description="展会总结报告")
public class ActiveMeetReport implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键*/
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    private Long id;

    /**活动名称*/
    @ApiModelProperty(value = "活动名称")
    private String activeName;

    /**开始日期*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "开始日期")
    private Date startDate;

    /**结束日期*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "结束日期")
    private Date endDate;

    /**状态,0：待总结，1：已总结*/
    @ApiModelProperty(value = "状态,0：待总结，1：已总结")
    private String status;

    /**总服务人数*/
    @ApiModelProperty(value = "总服务人数")
    private Long servicePersonnel;

    /**投诉数量*/
    @ApiModelProperty(value = "投诉数量")
    private Long complaintsTotal;

    /**建议数量*/
    @ApiModelProperty(value = "建议数量")
    private Long recommendedTotal;

    /**设备故障数*/
    @ApiModelProperty(value = "设备故障数")
    private Long deviceFailuresTotal;

    /**总用电量*/
    @ApiModelProperty(value = "总用电量")
    private Double consumptionElectricity;

    /**单人次能耗*/
    @ApiModelProperty(value = "单人次能耗")
    private Double personEnergyConsumption;

    /**展会天数*/
    @ApiModelProperty(value = "展会天数")
    private Long dayNumber;

    /**总客流*/
    @ApiModelProperty(value = "总客流")
    private Long passengerFlow;

    /**峰值客流*/
    @ApiModelProperty(value = "峰值客流")
    private Long peakFlow;

    /**参展商数*/
    @ApiModelProperty(value = "参展商数")
    private Long exhibitors;
}
