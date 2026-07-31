package org.jeecg.modules.fwbz.patorlPlan.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.modules.fwbz.entity.BaseEntity;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 巡更计划
 * @Author: jeecg-boot
 * @Date:   2026-07-31
 * @Version: V1.0
 */
@Data
@TableName("table_patrol_plan")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(value="table_patrol_plan对象", description="巡更计划")
public class PatrolPlan extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**计划名称*/
    @Excel(name = "计划名称", width = 15)
    @ApiModelProperty(value = "计划名称")
    private String planName;

    /**巡更路线*/
    @Excel(name = "巡更路线", width = 15)
    @ApiModelProperty(value = "巡更路线")
    private String patrolRoute;

    /**执行周期*/
    @Excel(name = "执行周期", width = 15)
    @ApiModelProperty(value = "执行周期")
    private String executionCycle;

    /**下次执行*/
    @Excel(name = "下次执行", width = 15)
    @ApiModelProperty(value = "下次执行")
    private String nextExecution;

    /**状态*/
    @Excel(name = "状态", width = 15)
    @ApiModelProperty(value = "状态")
    @TableField("staus")
    private Integer status;
}
