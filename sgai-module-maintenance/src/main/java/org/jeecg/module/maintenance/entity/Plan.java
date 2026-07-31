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
@TableName("device_maintenance_plan")
public class Plan extends BaseEntity{

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "计划编码")
    private String planCode;

    @ApiModelProperty(value = "计划名称")
    private String name;

    @ApiModelProperty(value = "任务名称")
    private String taskTypeName;

    @ApiModelProperty(value = "计划状态")
    private String planState;

    @ApiModelProperty(value = "任务类型id")
    private Long taskTypeId;

    @ApiModelProperty(value = "执行人")
    private String executor;

    @ApiModelProperty(value = "负责人")
    private String principal;

    @ApiModelProperty(value = "负责组")
    private String principalGroup;

    @ApiModelProperty(value = "负责组Ids")
    private String principalGroupId;

    @ApiModelProperty(value = "区域")
    private String area;

    @ApiModelProperty(value = "循环周期")
    private String cycle;

    @ApiModelProperty(value = "创建人")
    private String creatorName;

    @ApiModelProperty(value = "创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    @ApiModelProperty(value = "计划开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planBeginTime;

    @ApiModelProperty(value = "计划结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planEndTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "实际开始时间")
    private LocalDateTime realBeginTime;

    @ApiModelProperty(value = "实际结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime realEndTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "实际响应时间")
    private LocalDateTime realResponseTime;

    @ApiModelProperty("是否发现问题")
    private boolean question;

    @ApiModelProperty(value = "部门全路径")
    private String department;

    @ApiModelProperty(value = "部门全路径")
    private Long departmentId;

    @ApiModelProperty("维保类型")
    private String  weibaoType;

    @ApiModelProperty("派发时间")
    private LocalDateTime sendTime;
    @ApiModelProperty("标签类型")
    private String labelType;

}
