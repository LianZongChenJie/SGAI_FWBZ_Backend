package org.jeecg.modules.fwbz.complaint.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * @Description: 投诉建议处理记录
 * @Author: jeecg-boot
 * @Date:   2026-08-06
 * @Version: V1.0
 */
@Data
@TableName("table_complaint_record")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="table_complaint_record对象", description="投诉建议处理记录")
public class ComplaintRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "记录ID")
    private Long id;

    @ApiModelProperty(value = "投诉建议ID")
    private Long complaintId;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "处理日期")
    private Date handleDate;

    @ApiModelProperty(value = "处理时间，格式 HH:mm:ss")
    private String handleTime;

    @ApiModelProperty(value = "处理内容")
    private String handleContent;

    @ApiModelProperty(value = "变更前状态")
    private String statusFrom;

    @ApiModelProperty(value = "变更后状态")
    private String statusTo;

    @ApiModelProperty(value = "处理人")
    private String handler;

    @ApiModelProperty(value = "创建时间")
    private Date gmtCreate;

    @ApiModelProperty(value = "更新时间")
    private Date gmtModified;
}
