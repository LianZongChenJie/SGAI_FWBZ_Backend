package org.jeecg.module.maintenance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("event_operate_record")
public class OperateRecord extends BaseEntity{

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "工单id")
    private Long orderId;

    @ApiModelProperty(value = "操作人id")
    private String operatorId;

    @ApiModelProperty(value = "操作人姓名")
    private String operatorName;

    @ApiModelProperty(value = "操作时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationTime;

    @ApiModelProperty(value = "事件操作编码")
    private String operationCode;

    @ApiModelProperty(value = "事件操作名称")
    private String operationName;

    @ApiModelProperty(value = "事件操作别名")
    private String operationShowName;

    @ApiModelProperty(value = "源状态编码")
    private String sourceStatusCode;

    @ApiModelProperty(value = "源状态名称")
    private String sourceStatusName;

    @ApiModelProperty(value = "目标状态编码")
    private String targetStatusCode;

    @ApiModelProperty(value = "目标状态名称")
    private String targetStatusName;

    @ApiModelProperty(value = "备注")
    private String remarks;

    @ApiModelProperty(value = "流程类型")
    private String flowCode;
}
