package org.jeecg.modules.fwbz.complaint.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 投诉建议处理请求DTO
 * @Author: jeecg-boot
 * @Date:   2026-08-06
 * @Version: V1.0
 */
@Data
@ApiModel(value="投诉建议处理请求对象", description="投诉建议处理请求")
public class ComplaintHandleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "投诉建议ID", required = true)
    private Long complaintId;

    @ApiModelProperty(value = "新状态", required = true)
    private String status;

    @ApiModelProperty(value = "处理内容", required = true)
    private String handleContent;

    @ApiModelProperty(value = "处理人")
    private String handler;
}
