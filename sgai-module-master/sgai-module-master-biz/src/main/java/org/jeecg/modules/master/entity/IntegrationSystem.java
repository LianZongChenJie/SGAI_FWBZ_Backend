package org.jeecg.modules.master.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("integration_system")
@ApiModel("对接系统")
public class IntegrationSystem {

    @TableId(type = IdType.NONE)
    @ApiModelProperty("主键uuid")
    private String id;

    @ApiModelProperty("系统名称")
    private String name;

    @ApiModelProperty("系统编码(唯一)")
    private String code;

    @ApiModelProperty("是否启用推送 0否1是")
    private Integer pushEnabled;

    @ApiModelProperty("推送目标URL")
    private String pushUrl;

    @ApiModelProperty("是否启用接收 0否1是")
    private Integer receiveEnabled;

    @ApiModelProperty("共享令牌")
    private String token;

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
