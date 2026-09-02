package org.jeecg.modules.fwbz.complaint.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 投诉建议信息
 * @Author: jeecg-boot
 * @Date:   2026-08-06
 * @Version: V1.0
 */
@Data
@TableName("table_complaint_info")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="table_complaint_info对象", description="投诉建议信息")
public class ComplaintInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "投诉建议ID")
    private Long id;

    @ApiModelProperty(value = "标题")
    private String title;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "日期")
    private Date complaintDate;

    @ApiModelProperty(value = "时间，格式 HH:mm:ss")
    private String complaintTime;

    @ApiModelProperty(value = "投诉类型ID，关联 table_complaint_type.id")
    private Long typeId;

    @ApiModelProperty(value = "内容")
    private String content;

    @ApiModelProperty(value = "来源")
    private String source;

    @ApiModelProperty(value = "处理人")
    private String handler;

    @ApiModelProperty(value = "状态")
    private String status;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建时间")
    private Date gmtCreate;

    @ApiModelProperty(value = "更新时间")
    private Date gmtModified;

    /** 类型名称（非数据库字段，用于列表展示） */
    @TableField(exist = false)
    @ApiModelProperty(value = "类型名称")
    private String typeName;
}
