package org.jeecg.modules.master.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("device_category")
@ApiModel("类别主数据")
public class DeviceCategory {

    @TableId(type = IdType.NONE)
    @ApiModelProperty("主键uuid")
    private String id;

    @ApiModelProperty("类别名称")
    private String name;

    @ApiModelProperty("类别全称")
    private String fullName;

    @ApiModelProperty("上级id，根为0")
    private String pid;

    @ApiModelProperty("创建人")
    private String createBy;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新人")
    private String updateBy;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("同级内排序，升序，小在前")
    private Integer sort;
}
