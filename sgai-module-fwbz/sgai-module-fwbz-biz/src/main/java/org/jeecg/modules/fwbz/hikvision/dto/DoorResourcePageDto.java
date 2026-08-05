package org.jeecg.modules.fwbz.hikvision.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 门禁点分页查询DTO
 *
 * @author fwbz
 */
@Data
public class DoorResourcePageDto {

    @ApiModelProperty(value = "当前页码")
    private int pageNo = 1;

    @ApiModelProperty(value = "每页大小")
    private int pageSize = 10;

    @ApiModelProperty(value = "资源名称（模糊查询）")
    private String name;

    @ApiModelProperty(value = "门禁点编号（精确查询）")
    private String doorNo;

    @ApiModelProperty(value = "区域名称（模糊查询）")
    private String regionName;

    @ApiModelProperty(value = "门状态，0-初始，1-开门，2-关门，3-离线（精确查询）")
    private String doorState;

    @ApiModelProperty(value = "接入协议（精确查询）")
    private String treatyType;

    @ApiModelProperty(value = "安装位置（模糊查询）")
    private String installLocation;
}
