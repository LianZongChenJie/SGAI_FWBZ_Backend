package org.jeecg.modules.fwbz.hikvision.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 门禁设备分页查询DTO
 *
 * @author fwbz
 */
@Data
public class AcsDevicePageDto {

    @ApiModelProperty(value = "当前页码")
    private int pageNo = 1;

    @ApiModelProperty(value = "每页大小")
    private int pageSize = 10;

    @ApiModelProperty(value = "资源名称（模糊查询）")
    private String name;

    @ApiModelProperty(value = "设备类型编码（精确查询）")
    private String devTypeCode;

    @ApiModelProperty(value = "区域名称（模糊查询）")
    private String regionName;

    @ApiModelProperty(value = "在线状态，0-离线，1-在线（精确查询）")
    private String online;

    @ApiModelProperty(value = "设备IP（模糊查询）")
    private String ip;
}
