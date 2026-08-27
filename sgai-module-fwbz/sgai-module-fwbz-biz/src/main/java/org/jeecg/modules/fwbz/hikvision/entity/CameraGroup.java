package org.jeecg.modules.fwbz.hikvision.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 摄像头分组信息表
 *
 * @author fwbz
 */
@Data
@TableName("table_camera_group")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "table_camera_group对象", description = "摄像头分组信息表")
public class CameraGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID（IOC平台分组ID） */
    @TableId(type = IdType.INPUT)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    /** 分组名称 */
    @ApiModelProperty(value = "分组名称")
    private String name;

    /** 分组描述 */
    @ApiModelProperty(value = "分组描述")
    private String description;

    /** 同级顺序 */
    @ApiModelProperty(value = "同级顺序")
    private Integer sortNum;

    /** 分组维度 */
    @ApiModelProperty(value = "分组维度")
    private String dimension;

    /** 父分组ID，根节点为0 */
    @ApiModelProperty(value = "父分组ID，根节点为0")
    private Long parentId;
}
