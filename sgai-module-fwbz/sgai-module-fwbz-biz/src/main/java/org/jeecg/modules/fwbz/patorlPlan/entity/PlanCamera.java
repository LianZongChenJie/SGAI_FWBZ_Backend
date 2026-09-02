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

/**
 * @Description: 巡更计划关联摄像头
 * @Author: jeecg-boot
 * @Date:   2026-07-31
 * @Version: V1.0
 */
@Data
@TableName("table_plan_camera")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="table_plan_camera对象", description="巡更计划关联摄像头")
public class PlanCamera implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键*/
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    private Long id;

    /**巡更计划ID*/
    @ApiModelProperty(value = "巡更计划ID")
    private Long planId;

    /**摄像头唯一编码*/
    @ApiModelProperty(value = "摄像头唯一编码")
    private String indexCode;

    /**摄像头名称（非数据库字段，联表查询）*/
    @TableField(exist = false)
    @ApiModelProperty(value = "摄像头名称")
    private String cameraName;

    /**播放地址（非数据库字段，运行中计划返回时随机赋值）*/
    @TableField(exist = false)
    @ApiModelProperty(value = "播放地址")
    private String url;
}
