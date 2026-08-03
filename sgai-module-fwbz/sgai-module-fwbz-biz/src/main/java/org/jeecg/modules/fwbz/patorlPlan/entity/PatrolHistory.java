package org.jeecg.modules.fwbz.patorlPlan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 巡更历史
 * @Author: jeecg-boot
 * @Date:   2026-08-03
 * @Version: V1.0
 */
@Data
@TableName("table_patrolHistory")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="table_patrolHistory对象", description="巡更历史")
public class PatrolHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键*/
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    private Long id;

    /**巡更计划ID*/
    @ApiModelProperty(value = "巡更计划ID")
    private Long patrolId;

    /**运行时间*/
    @ApiModelProperty(value = "运行时间")
    @TableField("run_time")
    private Date runTime;
}
