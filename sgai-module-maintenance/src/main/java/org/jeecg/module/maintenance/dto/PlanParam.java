package org.jeecg.module.maintenance.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecg.module.maintenance.entity.OperateRecord;
import org.jeecg.module.maintenance.entity.Plan;

@ApiModel("维保计划、操作记录 修改")
@Data
public class PlanParam  {

    @ApiModelProperty("操作记录")
    private OperateRecord operateRecord;

    @ApiModelProperty("维保计划详情")
    private Plan plan;

    @ApiModelProperty("维保计划id")
    private  Long planId;



}
