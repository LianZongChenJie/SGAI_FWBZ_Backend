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


/**
 * 维保计划详情
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("device_maintenance_plan_model_detail")
public class PlanModelDetail extends BaseEntity{

    /**主键.*/
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("计划模板主键")
    private Long planModelId;

    @ApiModelProperty("开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime start;

    @ApiModelProperty("是否已启用")
    private Boolean enableFlag;

}
