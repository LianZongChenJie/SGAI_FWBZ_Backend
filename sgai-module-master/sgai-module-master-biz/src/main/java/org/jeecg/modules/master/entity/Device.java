package org.jeecg.modules.master.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("device")
@ApiModel("设备主数据")
public class Device {

    @TableId(type = IdType.NONE)
    @ApiModelProperty("主键uuid")
    private String id;

    @ApiModelProperty("设备名称")
    private String name;

    @ApiModelProperty("类别id")
    private String categoryId;

    @ApiModelProperty("空间id")
    private String spaceId;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建人")
    private String createBy;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新人")
    private String updateBy;

    @ApiModelProperty("更新时间")
    private Date updateTime;
}
